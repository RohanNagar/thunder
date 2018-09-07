# Thunder Documentation

## Build Locally

1. Install `pipenv`

```bash
$ pip install --user pipenv
```

2. Install the dependencies

```bash
$ pipenv install
```

3. Enter the virtual environment

```bash
$ pipenv shell
```

4. Build the documentation and open it

```bash
$ make html
$ open _build/html/index.html
```

Or, to autobuild as you make changes

```bash
$ sphinx-autobuild . _build/html
```
