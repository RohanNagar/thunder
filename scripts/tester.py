import argparse
import json
import os
import sys

import thunder_requests.methods as requests

from pprint import pprint


if __name__ == '__main__':
    parser = argparse.ArgumentParser('Script to add a user via Thunder')

    # Add command line args
    parser.add_argument('-f', '--filename', type=str, default='user_details.json',
                        help='JSON file containing the user details')
    parser.add_argument('-e', '--endpoint', type=str, default='http://localhost:8080',
                        help='the base endpoint to connect to')
    parser.add_argument('-a', '--auth', type=str, default='application:secret',
                        help='authentication credentials to connect to the endpoint')
    parser.add_argument('-v', '--verbose', action='store_true',
                        help='increase output verbosity')
    args = parser.parse_args()

    # Separate auth
    auth = (args.auth.split(':')[0], args.auth.split(':')[1])

    # Read JSON file
    script = os.path.dirname(__file__)
    file_path = os.path.join(script, args.filename)
    with open(file_path) as f:
        user_details = json.load(f)

    # --- Define tests ---
    def create(data):
        print('Attempting to create a new user...')
        return requests.add_user(args.endpoint + '/users',
                                 authentication=auth,
                                 body=data,
                                 verbose=args.verbose)

    def get(data):
        print('Attempting to get the user...')
        return requests.get_user(args.endpoint + '/users',
                                 authentication=auth,
                                 params={'email': data['email']['address']},
                                 headers={'password': data['password']},
                                 verbose=args.verbose)

    def email(data):
        print('Attempting to send a verification email...')
        return requests.send_email(args.endpoint + '/verify',
                                   authentication=auth,
                                   params={'email': data['email']['address']},
                                   headers={'password': data['password']},
                                   verbose=args.verbose)

    def verify(data):
        print('Attempting to verify the created user...')
        return requests.verify_user(args.endpoint + '/verify',
                                    authentication=auth,
                                    params={'email': data['email']['address'],
                                            'token': data['email']['verificationToken']},
                                    headers={'password': data['password']},
                                    verbose=args.verbose)

    def update_field(data):
        print('Attempting to update the user\'s Facebook access token...')

        data['facebookAccessToken'] = 'NEWFacebookAccessToken'
        return requests.update_user(args.endpoint + '/users',
                                    authentication=auth,
                                    params={},
                                    body=data,
                                    headers={'password': data['password']},
                                    verbose=args.verbose)

    def update_email(data):
        print('Attempting to update the user\'s email address...')

        existing_email = data['email']['address']
        data['email']['address'] = 'newemail@gmail.com'
        return requests.update_user(args.endpoint + '/users',
                                    authentication=auth,
                                    params={'email': existing_email},
                                    body=data,
                                    headers={'password': data['password']},
                                    verbose=args.verbose)

    def delete(data):
        print('Attempting to delete the user...')
        return requests.delete_user(args.endpoint + '/users',
                                    authentication=auth,
                                    params={'email': data['email']['address']},
                                    headers={'password': data['password']},
                                    verbose=args.verbose)

    test_pipeline = [create, get, email, verify, update_field, get, update_email, get, delete]

    # --- Run tests ---
    print('Running Full Thunder Test...')
    if args.verbose:
        print()
        print('Using user {}:'.format(user_details['email']['address']))
        pprint(user_details)

    print()

    result = user_details

    for test in test_pipeline:
        result = test(result)

        if not result:
            print('Attempting to clean up from failure by deleting user...')

            if not delete(user_details):
                print('** NOTE: Deletion failure means this user is still in the DB. **\n'
                      '** NOTE: Delete manually or with `thunder_requests/delete_user.py`. **')

            print('Aborting...')
            sys.exit(1)
