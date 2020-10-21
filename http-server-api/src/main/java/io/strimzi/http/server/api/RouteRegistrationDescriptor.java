package io.strimzi.http.server.api;

import io.vertx.ext.web.Router;

public class RouteRegistrationDescriptor {
    private final Router router;
    private final String mountPoint;

    private RouteRegistrationDescriptor(final String mountPoint, final Router router) {
        this.mountPoint = mountPoint;
        this.router = router;
    }

    public static RouteRegistrationDescriptor create(final String mountPoint, final Router router) {
        return new RouteRegistrationDescriptor(mountPoint, router);
    }

    public String mountPoint() {
        return this.mountPoint;
    }

    public Router router() {
        return this.router;
    }
}
