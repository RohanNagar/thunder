import argparse
import json
import thunder_requests.methods as requests

if __name__ == '__main__':
    parser = argparse.ArgumentParser('Script to add a user via Thunder')

    # Add command line args
    parser.add_argument('-f', '--filename', type=str, default='user_details.json',
                        help='JSON file containing the user details')
    parser.add_argument('-e', '--endpoint', type=str, default='http://localhost:8080',
                        help='the base endpoint to connect to')
    parser.add_argument('-v', '--verbosity', type=int, default=0, choices={0, 1},
                        help='0 = only success/failure. 1 = show HTTP response')
    parser.add_argument('-a', '--auth', type=str, default='application:secret',
                        help='authentication credentials to connect to the endpoint')
    args = parser.parse_args()

    # Separate auth
    auth = (args.auth.split(':')[0], args.auth.split(':')[1])

    # Read JSON
    with open(args.filename) as f:
        data = json.load(f)

    # Make POST request
    r = requests.add_user(args.endpoint + '/users',
                          authentication=auth,
                          body=data,
                          verbosity=args.verbosity)

    if not r:
        # Get out if the user was never added
        print('Ending Test...')

    # Make GET request
    r = requests.get_user(args.endpoint + '/users',
                          authentication=auth,
                          params={'username': data['username']},
                          headers={'password': data['password']},
                          verbosity=args.verbosity)

    data['facebookAccessToken'] = 'newFacebookAccessToken'

    # Make PUT request
    r = requests.update_user(args.endpoint + '/users',
                             authentication=auth,
                             body=data,
                             headers={'password': data['password']},
                             verbosity=args.verbosity)

    r = requests.get_user(args.endpoint + '/users',
                          authentication=auth,
                          params={'username': data['username']},
                          headers={'password': data['password']},
                          verbosity=args.verbosity)

    # Make DELETE request
    r = requests.delete_user(args.endpoint + '/users',
                             authentication=auth,
                             params={'username': data['username']},
                             headers={'password': data['password']},
                             verbosity=args.verbosity)
