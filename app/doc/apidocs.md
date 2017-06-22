#Tutor

## GET /tutor/:id

Gets a single tutor data. Returns a JSON document like this:

```json
  {
    "username": "foo",
    "fullname": "Foo Bar"
  }
```
## PUT /tutor/:id

Saves a new tutor. Accepts a JSON document like this: 

```json
  {
    "username": "foo",
    "password": "somepasswd",
    "fullname": "Foo Bar"
  }
```

And returns a simple JSON document with a `status` field, that can be either `"success"` or `"failure"`. In case of failure, a `"message"` field will also be returned.

# Actions

## POST /action/login-tutor

Perform authentication for a given tutor, and returns a token in case of success. Accepts a JSON document like the following:

```json
 {
   "username": "foo",
   "password": "somepasswd"
 }
```

It will return a JSON document, like this:

```json
  {
    "authenticated": true,
    "token": "somerandomtokenhash"
  }
```