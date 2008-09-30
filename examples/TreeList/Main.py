import sys
import Usage, AllProjects, PrintProjects

if __name__ == "__main__":
    try:
        host = sys.argv[1]
        user = sys.argv[2]
        pasw = sys.argv[3]
    except:
        Usage.usage()

    client = omero.client(host)
    factory = client.createSession(user, pasw)
    projects = AllProjects.getProjects(factory.getQueryService(), user)
    PrintProjects.print_(projects)

