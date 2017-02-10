import argparse
import requests
import json

from pprint import pprint

if __name__ == '__main__':
    parser = argparse.ArgumentParser('Script to add a user via Thunder')

    # Add command line args
    parser.add_argument('filename', type=str, help='a JSON file containing the user details')
    parser.add_argument('-e', '--endpoint', type=str, default='http://localhost:8080', help='the base endpoint to connect to')
    parser.add_argument('-v', '--verbosity', type=int, default=0, choices={0, 1}, help='0 = only success/failure. 1 = show HTTP response')
    parser.add_argument('-a', '--auth', type=str, default='application:secret', help='authentication credentials to connect to the endpoint')
    args = parser.parse_args()

    # Seperate auth
    auth = (args.auth.split(':')[0], args.auth.split(':')[1])

    # Read JSON
    with open(args.filename) as f:
        data = json.load(f)

    # Make request
    r = requests.post(args.endpoint + '/users', auth=auth, json=data)

    if r.status_code == requests.codes.created:
        print('Successfully created a new user.')

        if args.verbosity == 1:
            print('\nResponse:')
            try:
                pprint(r.json())
            except:
                print(r.text)
    else:
        print('An error occurred. Please try again.')

        if args.verbosity == 1:
            print('\nDetails:')
            print(r.text)
