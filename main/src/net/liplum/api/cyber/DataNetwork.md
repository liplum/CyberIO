## Design

Every data network node(is hereinafter referred to as Node) can only link with other nodes in four cardinal directions.

0->Right

1->Top

2->Left

3->Right

Every side can only link with one another node.

## Sync

The linking is processed automatically while updating.

Since player can't link them manually and updating is continous, it doesn't need any synchronization.

The class "SideLinks" aims at recording the links and is used for checking whether this side has been already linked.
