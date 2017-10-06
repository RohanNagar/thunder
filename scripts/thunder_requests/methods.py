import requests

from pprint import pprint


# Adds a user to the database via Thunder
def add_user(endpoint, authentication, body, verbose=False):
    try:
        r = requests.post(endpoint, auth=authentication, json=body)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

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
    try:
        r = requests.get(endpoint, auth=authentication, params=params, headers=headers)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

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
def update_user(endpoint, authentication, params, body, headers, verbose=False):
    try:
        r = requests.put(endpoint, auth=authentication, params=params, json=body, headers=headers)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

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
    try:
        r = requests.delete(endpoint, auth=authentication, params=params, headers=headers)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

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
