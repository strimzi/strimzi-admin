/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.http.server.registration;

import io.vertx.ext.web.Router;

/**
 * Contains a Vert.x {@link Router} and a mountPoint which acts as a path off the root path
 * under which the routes on the Router will be mounted.
 */
public class RouteRegistrationDescriptor {
    private final Router router;
    private final String mountPoint;

    private RouteRegistrationDescriptor(final String mountPoint, final Router router) {
        this.mountPoint = mountPoint;
        this.router = router;
    }

    /**
     * Factory class to create a RouteRegistrationDescriptor
     * @param mountPoint the path under which the routes will be mounted
     * @param router the Vert.x Router containing the routes to be mounted
     * @return a RouteRegistrationDescriptor containing the mountpoint and the Router.
     */
    public static RouteRegistrationDescriptor create(final String mountPoint, final Router router) {
        return new RouteRegistrationDescriptor(mountPoint, router);
    }

    /**
     * Retrieve the mount point
     * @return a String containing the mount point
     */
    public String mountPoint() {
        return this.mountPoint;
    }

    /**
     * Retrieve the Router
     * @return the Vert.x Router containing the routes to be added to the mount point
     */
    public Router router() {
        return this.router;
    }
}
