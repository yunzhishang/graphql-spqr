# GraphQL SPQR

GraphQL SPQR (GraphQL Schema Publisher & Query Resolver) is a simple to use library for rapid development of GraphQL APIs in Java.

[![Join the chat at https://gitter.im/leangen/Lobby](https://badges.gitter.im/leangen/Lobby.svg)](https://gitter.im/leangen/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/leangen/graphql-spqr.svg?branch=master)](https://travis-ci.org/leangen/graphql-spqr)

## Example

The example will use annotations provided by GraphQL SPQR itself, but these are optional and the mapping is completely configurable,
enabling existing services to be exposed through GraphQL without modification.

Service class:

    class UserService {
    
        @GraphQLQuery(name = "user")
        public User getById(@GraphQLArgument(name = "id") Integer id) {
          ...
        }
    }
    
Domain class:

    public class User<T> implements Person {

        @GraphQLQuery(name = "name", description = "A person's name")
        public String name;
    
        @GraphQLQuery(name = "id", description = "A person's id")
        public Integer id;
    
        @GraphQLQuery(name = "regDate", description = "Date of registration")
        public Date registrationDate;
    
        @Override
        public String getName() {
            return name;
        }
    
        public Integer getId() {
            return id;
        }
    
        public Date getRegistrationDate() {
            return registrationDate;
        }
    }
    
Exposing the service:

    UserService userService = new UserService(); //could also be injected by Spring or another framework
    GraphQLSchemaGenerator schema = new GraphQLSchemaGenerator()
        .withQuerySourceSingleton(userService) //more services can be added the same way
        .generate();
    GraphQL graphQL = new GraphQL(schema);
    
    //keep the reference to GraphQL instance and execute queries against it.
    //this operation selects a user by ID and requests name and regDate fields only
    ExecutionResult result = graphQL.execute(   
        "{ user (id: 123) {
            name,
            regDate
        }}");
