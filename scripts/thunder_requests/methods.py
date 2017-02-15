import requests

from pprint import pprint


# Adds a user to the database via Thunder
def add_user(endpoint, authentication, body, verbose=False):
    r = requests.post(endpoint, auth=authentication, json=body)

    if r.status_code == requests.codes.created:
        print('Successfully created a new user.')

        if verbose:
            print('Response:')
            try:
                pprint(r.json())
            except:
                print(r.text)

        print()
        return True
    else:
        print('An error occurred while creating.')

        if verbose:
            print('Details:')
            print(r.text)

        print()
        return False


# Retrieves a user from the database via Thunder
def get_user(endpoint, authentication, params, headers, verbose=False):
    r = requests.get(endpoint, auth=authentication, params=params, headers=headers)

    if r.status_code == requests.codes.ok:
        print('Successfully retrieved the user.')

        if verbose:
            print('Response:')
            try:
                pprint(r.json())
            except:
                print(r.text)

        print()
        return True
    else:
        print('An error occurred while retrieving.')

        if verbose:
            print('Details:')
            print(r.text)

        print()
        return False


# Updates a user in the database via Thunder
def update_user(endpoint, authentication, body, headers, verbose=False):
    r = requests.put(endpoint, auth=authentication, json=body, headers=headers)

    if r.status_code == requests.codes.ok:
        print('Successfully updated the user.')

        if verbose:
            print('Response:')
            try:
                pprint(r.json())
            except:
                print(r.text)

        print()
        return True
    else:
        print('An error occurred while updating.')

        if verbose:
            print('Details:')
            print(r.text)

        print()
        return False


# Deletes a user from the database via Thunder
def delete_user(endpoint, authentication, params, headers, verbose=False):
    r = requests.delete(endpoint, auth=authentication, params=params, headers=headers)

    if r.status_code == requests.codes.ok:
        print('Successfully deleted the user.')

        if verbose:
            print('Response:')
            try:
                pprint(r.json())
            except:
                print(r.text)

        print()
        return True
    else:
        print('An error occurred while deleting.')

        if verbose:
            print('Details:')
            print(r.text)

        print()
        return False
