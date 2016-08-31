from django.http import Http404

import omero
from omero.rtypes import wrap
from omeroweb.webclient.decorators import login_required, render_response


@login_required()
@render_response()
def index(request, conn=None, **kwargs):
    """
    Home page shows a list of Projects from all of our groups
    """

    myGroups = list(conn.getGroupsMemberOf())

    # Need a custom query to get 1 (random) image per Project
    queryService = conn.getQueryService()
    params = omero.sys.Parameters()
    params.theFilter = omero.sys.Filter()
    params.theFilter.limit = wrap(5)

    groups = []
    for g in myGroups:
        conn.SERVICE_OPTS.setOmeroGroup(g.id)
        projects = []
        images = list(conn.getObjects("Image", params=params))
        groups.append({'id': g.getId(),
                'name': g.getName(),
                'description': g.getDescription(),
                'images': images})

    context = {'template': "webgallery/index.html"}     # This is used by @render_response
    context['groups'] = groups

    return context


@login_required()
@render_response()
def show_group(request, groupId, conn=None, **kwargs):
    conn.SERVICE_OPTS.setOmeroGroup(groupId)

    s = conn.groupSummary(groupId)
    group_owners = s["leaders"]
    group_members = s["colleagues"]
    group = conn.getObject("ExperimenterGroup", groupId)

    # Get NEW user_id, OR current user_id from session OR 'All Members' (-1)
    user_id = request.REQUEST.get('user_id', request.session.get('user_id', -1))
    userIds = [u.id for u in group_owners]
    userIds.extend([u.id for u in group_members])
    user_id = int(user_id)
    if user_id not in userIds and user_id is not -1:        # Check user is in group
        user_id = -1
    request.session['user_id'] = int(user_id)    # save it to session
    request.session.modified = True

    queryService = conn.getQueryService()
    params = omero.sys.Parameters()
    params.theFilter = omero.sys.Filter()
    params.theFilter.limit = wrap(1)
    params.map = {}
    query = "select i from Image as i"\
            " left outer join i.datasetLinks as dl join dl.parent as dataset"\
            " left outer join dataset.projectLinks as pl join pl.parent as project"\
            " where project.id = :pid"

    if user_id == -1:
        user_id = None
    projects = []
    for p in conn.listProjects(eid=user_id):      # Will be from active group, owned by user_id (as perms allow)
        pdata = {'id': p.getId(), 'name': p.getName()}
        pdata['description'] = p.getDescription()
        pdata['owner'] = p.getDetails().getOwner().getOmeName()
        # Look-up a single image
        params.map['pid'] = wrap(p.id)
        img = queryService.findByQuery(query, params, conn.SERVICE_OPTS)
        if img is not None:
            pdata['image'] = {'id':img.id.val, 'name':img.name.val}
        projects.append(pdata)

    context = {'template': "webgallery/show_group.html"}
    context['group'] = group
    context['group_owners'] = group_owners
    context['group_members'] =group_members
    context['projects'] = projects

    return context


@login_required()
@render_response()
def show_project(request, projectId, conn=None, **kwargs):
    """
    Show a project
    """

    project = conn.getObject("Project", projectId)

    if project is None:
        raise Http404

    # Set a limit to grab 5 images from each Dataset
    params = omero.sys.Parameters()
    params.theFilter = omero.sys.Filter()
    params.theFilter.limit = wrap(5)

    datasets = []
    for ds in project.listChildren():
        # want to display 5 images from each dataset
        images = ds.listChildren(params=params)
        datasets.append({"id": ds.getId(),
                "name": ds.getName(),
                "description": ds.getDescription(),
                "images": images})

    context = {'template': "webgallery/show_project.html"}
    context['project'] = project
    context['datasets'] = datasets

    return context


@login_required()
@render_response()
def show_dataset(request, datasetId, conn=None, **kwargs):
    """
    Show a dataset
    """

    dataset = conn.getObject("Dataset", datasetId)

    if dataset is None:
        raise Http404

    context = {'template': "webgallery/show_dataset.html"}
    context['dataset'] = dataset

    return context


@login_required()
@render_response()
def show_image(request, imageId, conn=None, **kwargs):
    """
    Show an image
    """

    image = conn.getObject("Image", imageId)

    if image is None:
        raise Http404

    tags = []
    for ann in image.listAnnotations():
        print ann, ann.__class__.__name__, type(ann)
        if isinstance(ann, omero.gateway.TagAnnotationWrapper):
            tags.append(ann)

    context = {'template': "webgallery/show_image.html"}
    context['image'] = image
    context['tags'] = tags

    return context
