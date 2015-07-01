# JAX-RS API Versioning #

[![Build Status](https://travis-ci.org/CrosstreeLabs/jaxrs-versioning.svg)](https://travis-ci.org/CrosstreeLabs/jaxrs-versioning)[![Coverage Status](https://coveralls.io/repos/CrosstreeLabs/jaxrs-versioning/badge.svg)](https://coveralls.io/r/CrosstreeLabs/jaxrs-versioning)

The JAX-RS API Versioning library provides a set of JAX-RS providers, as well as
some useful interfaces for creating a versioned API. Message body readers and 
writers currently exist for Jackson 1 (JSON), Jackson 2 (JSON/XML), and GSON
serialization and deserialization.

Version your API by creating a set of value objects that represent the data to
be transferred into or out of your application.

    @Versioned(version = 1, contentTypes = {"application/vnd.crosstreelabs.user"})
    public class User implements ValueObject {
        public String name;
        public String username;
        public String email;
        ...
    }

You'll notice that the `@Versioned` annotation takes both a version number as
well as one or more content types. Between these two parameters, the entire
content type is determined.

Once you've created your value objects, all you need to do is add the message
body reader/writer of your choice in order to enable seamless serialization and
deserialization of your value objects during operation. Add any of the following
in order to set up proper de/serialization:

1. Jackson1JsonValueObjectReaderWriter
1. Jackson2JsonValueObjectReaderWriter
1. GsonJsonValueObjectReaderWriter
1. Jackson2XmlValueObjectReaderWriter

Or feel free to create your own.

    public class MyJsonValueObjectReaderWriter extends AbstractValueObjectReaderWriter {
        ...
    }

Once this is done, create your resources:

    public class UserResource {
        @GET
        @Path("/{id}")
        public User get(@PathParam("id") String id) {
            ...
            return user;
        }
        @POST
        public Response create(User user) {
            ...
        }
    }

# Library Integration #

Currently, the versioning library supports Jackson1, Jackson2, and GSON. It does
not, however, force a dependence on them. If you wish to use a particular
integration, you must include the library in your project. 

# Validation #

The library supports `javax.validation` if present. If the `javax.validation`
library is available, you can annotate your value objects appropriately, and
force validation when a value object is required.

    public class User implements ValueObject {
        @NotNull
        public String name;
        @NotNull
        public String username;
        @NotNull
        public String email;
    }

Any time you require the value object to be validated on submission, you simply
annotate the parameter with the `@Valid` annotation:

    @POST
    public Response create(@Valid User user) {
        ...
    }

Any validation errors will result in a `400 Bad Request` response being sent to
the client, along with a message describing the cause of the issue.