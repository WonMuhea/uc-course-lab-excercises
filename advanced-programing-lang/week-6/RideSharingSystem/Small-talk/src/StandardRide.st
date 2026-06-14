"========================================================================
 @class StandardRide
 @brief Concrete subclass for low-cost economy rides
 ========================================================================"

Ride subclass: StandardRide [

    <category: 'RideSharingSystem'>

    initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist [
        super initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist.
    ]

    calculateFare [
        ^ 2.50 + (distance * 1.25)
    ]

    rideDetails [
        | fare wholePart fractionPart |
        super rideDetails.
        
        fare := self calculateFare.
        wholePart := (fare * 100) rounded // 100.
        fractionPart := (fare * 100) rounded \\ 100.
        FileStream stdout nextPutAll: (' [Type: Standard, Fare: $', 
            wholePart printString, '.', (fractionPart < 10 ifTrue: [ '0' ] ifFalse: [ '' ]), 
            fractionPart printString, ']'); nl; flush.
    ]
]
