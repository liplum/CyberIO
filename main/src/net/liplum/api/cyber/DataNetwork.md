# Side Links

## Design

Every data network node(is hereinafter referred to as Node) can only link with other nodes in four cardinal directions.

0->Right

1->Top

2->Left

3->Right

```
         ▲
        1│y
         │
 2       │       0
─────────┼─────────►
         │         x
         │
        3│
```

Every side can only link with one another node.

## Sync

The linking is processed automatically while updating.

Since player can't link them manually and updating is continuous, it doesn't need any synchronization.

The class "SideLinks" aims at recording the links and is used for checking whether this side has been already linked.

# Transfer

## Design

#### Request

Some kinds of network node is interactable. Player can click it and select one payload in network to retrieve it.

The source of payload will send it to this place node by node. The network will find a path from source to applicant and
assign every node on this path to transfer payload.

Request shouldn't block whole network, in other words, the path only affects who have got the payload data, but the rest
of nodes still work as normal, of course, they may have other requests.

The request doesn't limit the subject and every block or unit except for player can post a new request to get any data
it wants, however, only the block can transfer payload data.

Request is immutable, at the same time, one block can only require one payload.

If the request no longer exists, more specifically, the source of that payload is removed or disconnected with this
network, the request should be removed.

#### Path

Path only contains the position information of nodes.

Since the network is changeable, any operation such as breaking a node block or adding a new node into network will
change the structure, the path isn't immutable.

The network will regenerate all paths when the network is changed.

## Implementation

#### Request

Because request relies on the node block, so we need a unique resource locator:

````
network node:
    request: DataLocator
````

Data Locator represents a unique payload and its host block in the map.

It doesn't change even though the host block is removed or the payload data has been sent to next node.

```
payload data:
    payload: Payload
    locator: DataLocator
```

It will be generated when the payload data is created so that it can be synchronized via network cable. The format is
below.

```
// creator: PackedPos // which block craeted this payload data
id: Int // which the total
```

the `id` is generated on two side but the logical server will send datapack and overwrite the wrong id.

#### Path

Every node contains a reference of Path is unrealistic, and it's hard to track all Path objects, since they are
allocated by object pool.

NetworkUpdater will iterate all nodes and reroute which node they would soon transfer to when updating.

```
network updater:
    update:
    1. Check if there is any new transfer task (request: Data ID -> Transfer task)
    | If yes, generate and cache the path
    2. Assgin all nodes transfer tasks if they should.
    | Iterate all active paths and find who got the data another node wants, assign it to transfer to its next node
```

Every node should be synchronized.

- Payload Data List
    * Data
        * Payload
        * ID // data locator
- Oriented Side
- Sending Progress