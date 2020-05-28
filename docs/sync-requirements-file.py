import configparser

def main():
  parser = configparser.ConfigParser()
  parser.read("Pipfile")

  packages = "packages"
  with open("readthedocs-requirements.txt", "w") as f:
    for key in parser[packages]:
      value = parser[packages][key]

      if key == "sphinxcontrib-httpexample":
        f.write(key + value.replace("\"", "") + "\n")

if __name__ == "__main__":
  main()
