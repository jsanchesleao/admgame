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

# Game

These endpoins control game instances. Every game instance is tied to a tutor.

## GET /tutor/:tutorid/game

This endpoint returns all games of a given tutor. A token for the tutor id is required.

It responds a JSON document like the following

```json
{
    "games": [
        {
            "tutor": "foo",
            "title": "First Game",
            "key": "first-game",
            "teams": [
                {
                    "key": "gamma-phi",
                    "name": "Gamma Phi",
                    "stock": {
                        "product": 0,
                        "wrapper": 0
                    },
                    "cash": 0
                }
            ]
        }
    ]
}
```

## GET /tutor/:tutorid/game/:gameid

Returns a single game instance. An authorization token for the tutor is required. The document is like the following:

```json
{
  "tutor": "foo",
  "title": "First Game",
  "key": "first-game",
  "teams": [
      {
          "key": "delta-kappa",
          "name": "Delta Kappa",
          "stock": {
              "product": 0,
              "wrapper": 0
          },
          "cash": 0
      }
  ]
}
```

## POST /tutor/:tutorid/game

Creates a game for a given tutor. It accepts a JSON document like this:

```json
{
  "title": "My First Game"
}
```

It will return, in case of success, a document like this:

```json
{"id": "my-first-game"}
```

If a game with the same key (which is based on the title) exists, the call will fail, returning status 422 and the following document

```json
{"type": "game-with-existing-key"}
```

## GET /tutor/:tutorid/game/:gameid/team

Lists all teams of a given game instance. Requires tutor token. The document returned is like this:

```json
{
  "teams": [
      {
          "key": "delta-kappa",
          "name": "Delta Kappa",
          "stock": {
              "product": 0,
              "wrapper": 0
          },
          "cash": 0
      }
  ]
}
```

## GET /tutor/:tutorid/game/:gameid/team/:teamid

Returns data for a single team in a given game. A token for either the tutor or the team is required. The returned document is like this:

```json
{
    "key": "delta-kappa",
    "name": "Delta Kappa",
    "stock": {
        "product": 0,
        "wrapper": 0
    },
    "cash": 0
}
```

## POST /tutor/:tutorid/game/:gameid/team

Creates a team and binds it to a game. Requires tutor token. The request is a JSON document in the following format:

```json
{
  "name": "Delta Kappa",
  "password": "somepasswd"
}
```

If succeeded, the returned document will look like this:

```json
{
  "id": "delta-kappa"
}
```

If a team with the same id (which is based on the name, like games do) exists, the call will fail, returning status 422 and the following document

```json
{"type": "team-with-existing-key"}
```

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

## POST /action/login-team

Perform authentication for a given team of an instance of a game, and returns a token in case of success. Accepts a JSON document like the following:

```json
 {
   "tutor": "foo",
   "game": "my-game",
   "team": "delta-kappa",
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