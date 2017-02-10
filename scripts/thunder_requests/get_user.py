import argparse
import hashlib
import requests

from pprint import pprint


# Retrieves a user from the database via Thunder
def get_user(endpoint, authentication, params, headers, verbosity=0):
    r = requests.get(endpoint, auth=authentication, params=params, headers=headers)

    if r.status_code == requests.codes.ok:
        print('Successfully retrieved the user.')

        if verbosity == 1:
            print('\nResponse:')
            try:
                pprint(r.json())
            except:
                print(r.text)
    else:
        print('An error occurred while retrieving.')

        if verbosity == 1:
            print('\nDetails:')
            print(r.text)


if __name__ == '__main__':
    parser = argparse.ArgumentParser('Script to get a user via Thunder')

    # Add command line args
    parser.add_argument('username', type=str,
                        help='username of the user to retrieve')
    parser.add_argument('password', type=str,
                        help='password of the user to retrieve')
    parser.add_argument('-e', '--endpoint', type=str, default='http://localhost:8080',
                        help='the base endpoint to connect to')
    parser.add_argument('-v', '--verbosity', type=int, default=0, choices={0, 1},
                        help='0 = only success/failure. 1 = show HTTP response')
    parser.add_argument('-a', '--auth', type=str, default='application:secret',
                        help='authentication credentials to connect to the endpoint')
    args = parser.parse_args()

    # Hash password
    m = hashlib.md5()
    m.update(args.password.encode('utf-8'))
    password = m.hexdigest()

    # Separate auth
    auth = (args.auth.split(':')[0], args.auth.split(':')[1])

    # Make request
    get_user(args.endpoint + '/users',
             authentication=auth,
             params={'username': args.username},
             headers={'password': password},
             verbosity=args.verbosity)
