"========================================================================
 @class PremiumRide
 @brief Concrete subclass for luxury premium tier rides
 ========================================================================"

Ride subclass: PremiumRide [

    <category: 'RideSharingSystem'>

    initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist [
        super initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist asNumber asFloat.
    ]

    calculateFare [
        ^ 5.00 + (distance * 2.50)
    ]

    rideDetails [
        | fare totalCents wholePart fractionPart |
        super rideDetails.

        fare := self calculateFare.
        
        "Consolidated safe formatting logic"
        totalCents := (fare * 100) rounded.
        wholePart := totalCents // 100.
        fractionPart := (totalCents \\ 100) abs.

        FileStream stdout nextPutAll: (' [Type: PREMIUM ⭐, Fare: $', 
            wholePart printString, '.', 
            (fractionPart < 10 ifTrue: [ '0' ] ifFalse: [ '' ]), 
            fractionPart printString, ']'); nl; flush.
    ]
]
