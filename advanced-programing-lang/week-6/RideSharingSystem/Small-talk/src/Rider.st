"========================================================================
 @class Rider
 @brief Manages client account and personal trip history collections
 ========================================================================"

Object subclass: Rider [

    | riderID name requestedRides |

    <category: 'RideSharingSystem'>

    initializeWithId: anId name: aName [
        riderID := anId.
        name := aName.
        requestedRides := OrderedCollection new.
    ]

    getID [ ^ riderID ]
    getName [ ^ name ]

    requestRide: aRide [
        requestedRides add: aRide.
    ]

    viewRides [

        | stdout |
        stdout := FileStream stdout.
        stdout nextPutAll: ('Rider History for ', name, ' (ID: ', riderID, '):'); nl; flush.
        requestedRides isEmpty ifTrue: [
            stdout nextPutAll: '  - [No ride history recorded]'; nl; flush.
            ^ self.
        ].
        requestedRides do: [ :ride |
            stdout nextPutAll: '  - '.
            ride rideDetails.
        ].
    ]
]
