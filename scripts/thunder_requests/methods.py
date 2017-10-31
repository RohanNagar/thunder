import requests

from pprint import pprint


class Method:
    POST = "POST"
    GET = "GET"
    PUT = "PUT"
    DELETE = "DELETE"
    VERIFY = "VERIFY"


def check_response(r, expected, method, verbose=False):
    if r.status_code == expected:
        print('Successfully completed method ' + method)

        if verbose:
            print('Response:')
            try:
                pprint(r.json())
            except Exception:
                print(r.text)

        print()
        return r.json()
    else:
        print('An error occurred while performing method ' + method)

        if verbose:
            print('Details:')
            print(r.text)

        print()
        return False


# Adds a user to the database via Thunder
def add_user(endpoint, authentication, body, verbose=False):
    try:
        r = requests.post(endpoint, auth=authentication, json=body)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

    return check_response(r, requests.codes.created, Method.POST, verbose)


# Verifies the user email address
def verify_user(endpoint, authentication, params, headers, verbose=False):
    try:
        r = requests.get(endpoint, auth=authentication, params=params, headers=headers)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

    return check_response(r, requests.codes.ok, Method.VERIFY, verbose)


# Retrieves a user from the database via Thunder
def get_user(endpoint, authentication, params, headers, verbose=False):
    try:
        r = requests.get(endpoint, auth=authentication, params=params, headers=headers)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

    return check_response(r, requests.codes.ok, Method.GET, verbose)


# Updates a user in the database via Thunder
def update_user(endpoint, authentication, params, body, headers, verbose=False):
    try:
        r = requests.put(endpoint, auth=authentication, params=params, json=body, headers=headers)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

    return check_response(r, requests.codes.ok, Method.PUT, verbose)


# Deletes a user from the database via Thunder
def delete_user(endpoint, authentication, params, headers, verbose=False):
    try:
        r = requests.delete(endpoint, auth=authentication, params=params, headers=headers)
    except requests.exceptions.RequestException:
        print('Unable to connect to the supplied endpoint.')
        return False

    return check_response(r, requests.codes.ok, Method.DELETE, verbose)
