"========================================================================
 @class StandardRide
 @brief Concrete subclass for low-cost economy rides
 ========================================================================"

Ride subclass: StandardRide [

    <category: 'RideSharingSystem'>

    initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist [
        super initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist asNumber asFloat.
    ]

    calculateFare [
        ^ 2.50 + (distance * 1.25)
    ]

    rideDetails [
        | fare totalCents wholePart fractionPart |
        super rideDetails.
        
        fare := self calculateFare.
        
        "Consolidated safe formatting logic"
        totalCents := (fare * 100) rounded.
        wholePart := totalCents // 100.
        fractionPart := (totalCents \\ 100) abs.

        FileStream stdout nextPutAll: (' [Type: Standard, Fare: $', 
            wholePart printString, '.', 
            (fractionPart < 10 ifTrue: [ '0' ] ifFalse: [ '' ]), 
            fractionPart printString, ']'); nl; flush.
    ]
]
