To run the program follow these steps:

make sure your in current directory on terminal before below steps

Step 1: Run the Lookup server and the Broker server on two seperate windows by typing
 "./lookup.sh <lookupport> " and  "./server.sh <serverport> <lookuphost> <lookupport>", 
here the Broker server is apecifying it's own port number and connecting to the host
and port number of the lookup server which need to be specified by you the client.

Step 2:  Start the Brokerclient by typing  "./client.sh <serverhost> <serverport> " where <serverhost>
<serverport> corresponds to the ip adress of the Lookup server and port is the port number with
which you executed the Lookup server

Step 3: Type "local <file>" where <file> can be either "nasdaq" or "tse" once sucessfully connected
you may query to recieve the number of locations for a given exchange by typing the name of the exchange
as specified in the files

Step 4: By first typing "local <file>" , you may also add <name>, remove <name>, update <name> <value>. 
Where <name> is the name of the echange you want to edit or add and <value> is the new value you want
 to use to update. This is done by simly using " <edit> <name> <value>" where <edit> can be update, remove or add
(note remove and add doesn't need a value and all newly added exchanges has location value of 0 until updated).
