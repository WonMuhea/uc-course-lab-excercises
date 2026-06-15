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

        | stdout totalEarnings safeRating |
        stdout := FileStream stdout.
        
        safeRating := self formatFloat: rating precision: 1.

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

        stdout nextPutAll: ('Total Driver Earnings: $', (self formatFloat: totalEarnings precision: 2)); nl; flush.
    ]

    "Private helper to format floats safely"
    formatFloat: aFloat precision: digits [
        | multiplier totalUnits wholePart fractionPart fractionStr |
        multiplier := (10 raisedTo: digits).
        totalUnits := (aFloat * multiplier) rounded.
        wholePart := totalUnits // multiplier.
        fractionPart := (totalUnits \\ multiplier) abs.
        
        fractionStr := fractionPart printString.
        [ fractionStr size < digits ] whileTrue: [ fractionStr := '0', fractionStr ].
        ^ wholePart printString, '.', fractionStr.
    ]
]
