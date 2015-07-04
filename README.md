## Testing
#### Scalatest
When working with functional test e.g. user registration, add new listing, it's best to use BDD-style test. One good
candidate is `FeatureSpec` e.g. `UserDAORelationshipSpec`

#### Testing Mysql
Your class need to extends `MySqlSpec`. This trait does all the setup and tear down database for each of your test class.
Note that inside a test class, test cases are executed sequentially but not out side of that class. Therefore it is
important to make sure all your test suites are isolated. We currently use different database for each suite and run
liquibase update to setup all tables.

#### Testing MongoDb