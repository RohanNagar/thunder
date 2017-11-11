import argparse
import json
import os
import sys

import thunder_requests.methods as requests

from pprint import pprint


def terminate():
    print('Aborting Test...')
    sys.exit(1)


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
        data = json.load(f)

    # --- Begin test ---
    print('Running Full Thunder Test...')
    if args.verbose:
        print()
        print('Using user {}:'.format(data['email']['address']))
        pprint(data)

    print()

    # Make POST request
    print('Attempting to create the new user...')
    data = requests.add_user(args.endpoint + '/users',
                             authentication=auth,
                             body=data,
                             verbose=args.verbose)

    if not data:
        terminate()

    # Make GET request
    print('Attempting to get the created user...')
    data = requests.get_user(args.endpoint + '/users',
                             authentication=auth,
                             params={'email': data['email']['address']},
                             headers={'password': data['password']},
                             verbose=args.verbose)

    # Send Email
    print('Attempting to send an email')
    data = requests.send_email(args.endpoint + '/verify',
                                authentication=auth,
                                params={'email': data['email']['address']},
                                headers={'password': data['password']},
                                verbose=args.verbose)

    # Verify
    print('Attempting to verify the created user...')
    data = requests.verify_user(args.endpoint + '/verify',
                                authentication=auth,
                                params={'email': data['email']['address'],
                                        'token': data['email']['verificationToken']},
                                headers={'password': data['password']},
                                verbose=args.verbose)

    if not data:
        terminate()

    # Update and make PUT request
    print('Attempting to update the user\'s Facebook access token...')

    data['facebookAccessToken'] = 'BRAND_NEW_FacebookAccessToken'
    data = requests.update_user(args.endpoint + '/users',
                                authentication=auth,
                                params={},
                                body=data,
                                headers={'password': data['password']},
                                verbose=args.verbose)

    if not data:
        terminate()

    # Ensure we can get the updated user
    print('Attempting to get the updated user...')
    data = requests.get_user(args.endpoint + '/users',
                             authentication=auth,
                             params={'email': data['email']['address']},
                             headers={'password': data['password']},
                             verbose=args.verbose)

    # Update email and make PUT request
    print('Attempting to update the user\'s email address...')

    existingEmail = data['email']['address']
    data['email']['address'] = 'newemail@gmail.com'
    data = requests.update_user(args.endpoint + '/users',
                                authentication=auth,
                                params={'email': existingEmail},
                                body=data,
                                headers={'password': data['password']},
                                verbose=args.verbose)

    if not data:
        terminate()

    # Make DELETE request
    print('Attempting to delete the user...')
    data = requests.delete_user(args.endpoint + '/users',
                                authentication=auth,
                                params={'email': data['email']['address']},
                                headers={'password': data['password']},
                                verbose=args.verbose)

    if not data:
        print('** NOTE: Deletion failure means this user is still in the DB. **\n'
              '** NOTE: Delete manually or with `thunder_requests/delete_user.py`. **')
