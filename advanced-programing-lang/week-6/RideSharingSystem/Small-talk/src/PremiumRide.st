"========================================================================
 @class PremiumRide
 @brief Concrete subclass for luxury premium tier rides
 ========================================================================"

Ride subclass: PremiumRide [

    <category: 'RideSharingSystem'>

    initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist [
        super initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist.
    ]

    calculateFare [
        ^ 5.00 + (distance * 2.50)
    ]

    rideDetails [
        | fare wholePart fractionPart |
        super rideDetails.

        fare := self calculateFare.
        wholePart := (fare * 100) rounded // 100.
        fractionPart := (fare * 100) rounded \\ 100.
        FileStream stdout nextPutAll: (' [Type: PREMIUM ⭐, Fare: $', 
            wholePart printString, '.', (fractionPart < 10 ifTrue: [ '0' ] ifFalse: [ '' ]), 
            fractionPart printString, ']'); nl; flush.
    ]
]
