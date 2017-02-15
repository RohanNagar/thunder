import argparse
import json
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
    with open(args.filename) as f:
        data = json.load(f)

    # --- Begin test ---
    print('Running Full Thunder Test...')
    if args.verbose:
        print('Using user {}:'.format(data['username']))
        pprint(data)

    print()

    # Make POST request
    r = requests.add_user(args.endpoint + '/users',
                          authentication=auth,
                          body=data,
                          verbose=args.verbose)

    if not r:
        # Get out if the user was never added
        print('Aborting Test...')

    # Make GET request
    r = requests.get_user(args.endpoint + '/users',
                          authentication=auth,
                          params={'username': data['username']},
                          headers={'password': data['password']},
                          verbose=args.verbose)

    data['facebookAccessToken'] = 'BRAND_NEW_FacebookAccessToken'

    # Make PUT request
    r = requests.update_user(args.endpoint + '/users',
                             authentication=auth,
                             body=data,
                             headers={'password': data['password']},
                             verbose=args.verbose)

    r = requests.get_user(args.endpoint + '/users',
                          authentication=auth,
                          params={'username': data['username']},
                          headers={'password': data['password']},
                          verbose=args.verbose)

    # Make DELETE request
    r = requests.delete_user(args.endpoint + '/users',
                             authentication=auth,
                             params={'username': data['username']},
                             headers={'password': data['password']},
                             verbose=args.verbose)

    if not r:
        print('** NOTE: Deletion failure means this user is still in the DB. **\n'
              '** NOTE: Delete manually or with `thunder_requests/delete_user.py`. **')
