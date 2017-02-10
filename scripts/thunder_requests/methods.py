import requests

from pprint import pprint


# Adds a user to the database via Thunder
def add_user(endpoint, authentication, body, verbosity=0):
    r = requests.post(endpoint, auth=authentication, json=body)

    if r.status_code == requests.codes.created:
        print('Successfully created a new user.')

        if verbosity == 1:
            print('Response:')
            try:
                pprint(r.json())
            except:
                print(r.text)

        print()
        return True
    else:
        print('An error occurred while creating.')

        if verbosity == 1:
            print('Details:')
            print(r.text)

        print()
        return False


# Retrieves a user from the database via Thunder
def get_user(endpoint, authentication, params, headers, verbosity=0):
    r = requests.get(endpoint, auth=authentication, params=params, headers=headers)

    if r.status_code == requests.codes.ok:
        print('Successfully retrieved the user.')

        if verbosity == 1:
            print('Response:')
            try:
                pprint(r.json())
            except:
                print(r.text)

        print()
        return True
    else:
        print('An error occurred while retrieving.')

        if verbosity == 1:
            print('Details:')
            print(r.text)

        print()
        return False


# Updates a user in the database via Thunder
def update_user(endpoint, authentication, body, headers, verbosity=0):
    r = requests.put(endpoint, auth=authentication, json=body, headers=headers)

    if r.status_code == requests.codes.ok:
        print('Successfully updated the user.')

        if verbosity == 1:
            print('Response:')
            try:
                pprint(r.json())
            except:
                print(r.text)

        print()
        return True
    else:
        print('An error occurred while updating.')

        if verbosity == 1:
            print('Details:')
            print(r.text)

        print()
        return False


# Deletes a user from the database via Thunder
def delete_user(endpoint, authentication, params, headers, verbosity=0):
    r = requests.delete(endpoint, auth=authentication, params=params, headers=headers)

    if r.status_code == requests.codes.ok:
        print('Successfully deleted the user.')

        if verbosity == 1:
            print('Response:')
            try:
                pprint(r.json())
            except:
                print(r.text)

        print()
        return True
    else:
        print('An error occurred while deleting.')

        if verbosity == 1:
            print('Details:')
            print(r.text)

        print()
        return False
