"========================================================================
 @class Ride
 @brief Abstract base class managing transactional ride properties
 ========================================================================"

Object subclass: Ride [

    | rideID pickupLocation dropoffLocation distance |

    <category: 'RideSharingSystem'>

    initializeWithId: anId pickup: pickup dropoff: dropoff distance: dist [
        rideID := anId.
        pickupLocation := pickup.
        dropoffLocation := dropoff.
        distance := dist.
    ]

    distance [ ^ distance ]
    rideID [ ^ rideID ]

    calculateFare [
        ^ self subclassResponsibility
    ]

    rideDetails [
        FileStream stdout nextPutAll: ('Ride [', rideID, '] from ', pickupLocation, ' to ', dropoffLocation, ' (', distance printString, ' miles)')
    ]
]
