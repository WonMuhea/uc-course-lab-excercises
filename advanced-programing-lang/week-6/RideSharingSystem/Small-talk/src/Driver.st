"========================================================================
 @class Driver
 @brief Tracks operator metrics and absolute payout ledgers
 ========================================================================"

Object subclass: Driver [

    | driverID name rating assignedRides |

    <category: 'RideSharingSystem'>

    initializeWithId: anId name: aName rating: aRating [
        driverID := anId.
        name := aName.
        rating := aRating.
        assignedRides := OrderedCollection new.
    ]

    getID [ ^ driverID ]
    getName [ ^ name ]
    getRating [ ^ rating ]

    addRide: aRide [
        assignedRides add: aRide.
    ]

    getDriverInfo [

        | stdout totalEarnings wholePart fractionPart safeRating |
        stdout := FileStream stdout.

        "Safe manual formatting for rating (1 decimal place) to avoid Float printString bug"
        wholePart := (rating * 10) rounded // 10.
        fractionPart := (rating * 10) rounded \\ 10.
        safeRating := wholePart printString, '.', fractionPart printString.

        stdout nextPutAll: ('Driver: ', name, ' (ID: ', driverID, ') | Rating: ', safeRating, '/5.0'); nl; flush.
        stdout nextPutAll: 'Completed Workload Ledger:'; nl; flush.
        
        assignedRides isEmpty ifTrue: [
            stdout nextPutAll: '  - [No trips processed]'; nl; flush.
            stdout nextPutAll: 'Total Driver Earnings: $0.00'; nl; flush.
            ^ self.
        ].
        
        totalEarnings := 0.0.
        assignedRides do: [ :ride |
            stdout nextPutAll: '  - '.
            ride rideDetails.
            totalEarnings := totalEarnings + ride calculateFare.
        ].

        "Safe manual formatting for earnings (2 decimal places)"
        wholePart := (totalEarnings * 100) rounded // 100.
        fractionPart := (totalEarnings * 100) rounded \\ 100.
        stdout nextPutAll: ('Total Driver Earnings: $', wholePart printString, '.', 
            (fractionPart < 10 ifTrue: [ '0' ] ifFalse: [ '' ]), fractionPart printString); nl; flush.
    ]
]
