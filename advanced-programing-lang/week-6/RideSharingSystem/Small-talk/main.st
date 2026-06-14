"========================================================================
 @file main.st
 @brief Interactive CLI execution kernel loading all dependencies
 ========================================================================"

PackageLoader fileInPackage: 'Regex'.

"Import our components sequentially out of the src folder"
FileStream fileIn: 'src/Ride.st'.
FileStream fileIn: 'src/StandardRide.st'.
FileStream fileIn: 'src/PremiumRide.st'.
FileStream fileIn: 'src/Rider.st'.
FileStream fileIn: 'src/Driver.st'.
FileStream fileIn: 'src/StorageManager.st'.

| globalRiders globalDrivers activeRider rideCounter stdin stdout choice addressRegex validateBlock getValidatedAddress handleRiderSession selectRandomDriver storage |

globalRiders := OrderedCollection new.
globalDrivers := OrderedCollection new.
rideCounter := 1.

stdin := FileStream stdin.
stdout := FileStream stdout.

storage := StorageManager new.
storage initializeWithFile: 'system_state.json'.
storage loadStateIntoRiders: globalRiders intoDrivers: globalDrivers.

addressRegex := '^(.+),\s*([A-Z]{2})\s+(\d{5})$'.

getValidatedAddress := [ :prompt |

    | address isValid |
    isValid := false.
    [ isValid ] whileFalse: [
        stdout nextPutAll: prompt; flush.
        address := stdin nextLine.
        address ifNil: [ address := '' ].
        (address matchRegex: addressRegex) 
            ifTrue: [ isValid := true ]
            ifFalse: [ stdout nextPutAll: '❌ Invalid! Format: [Street Name], [State] [Zipcode] (e.g., 123 Main St, VA 22314)'; nl; nl; flush ].
    ].
    address.
].

handleRiderSession := [

    | inputName targetRider nextID |
    stdout nextPutAll: '=== RIDER AUTHENTICATION MODULE ==='; nl; flush.
    stdout nextPutAll: 'Enter your Rider Name: '; flush.
    inputName := stdin nextLine.
    inputName ifNil: [ inputName := 'Guest' ].
    
    targetRider := globalRiders detect: [ :r | r getName = inputName ] ifNone: [ nil ].
    
    targetRider ifNil: [
        nextID := 'U', (globalRiders size + 101) printString.
        targetRider := Rider new initializeWithId: nextID name: inputName.
        globalRiders add: targetRider.
        storage saveStateRiders: globalRiders drivers: globalDrivers.
        stdout nextPutAll: ('👤 New rider profile created for ', inputName, ' (ID: ', nextID, ').'); nl; nl; flush.
    ] ifNotNil: [
        stdout nextPutAll: ('✅ Welcome back, ', inputName, '!'); nl; nl; flush.
    ].
    targetRider.
].

selectRandomDriver := [

    | rand index |
    globalDrivers isEmpty ifTrue: [
        globalDrivers add: (Driver new initializeWithId: 'D01' name: 'Carlos (Tesla Model 3)' rating: 4.9).
        globalDrivers add: (Driver new initializeWithId: 'D02' name: 'Diana (Toyota Prius)' rating: 4.7).
        globalDrivers add: (Driver new initializeWithId: 'D03' name: 'Evan (BMW M4)' rating: 5).
    ].
    rand := Random new.
    index := (rand next * globalDrivers size) floor + 1.
    globalDrivers at: index.
].

activeRider := handleRiderSession value.
"FIXED: Safe defensive initialization configuration guards active session tracking against drops"
activeRider ifNil: [ activeRider := Rider new initializeWithId: 'U101' name: 'Guest' ].

choice := '0'.
[ choice = '5' ] whileFalse: [
    stdout nextPutAll: '======================================'; nl.
    stdout nextPutAll: '    RIDE-SHARING PERSISTENT SYSTEM    '; nl.
    stdout nextPutAll: '======================================'; nl.
    stdout nextPutAll: ('Logged in as: ', activeRider getName); nl.
    stdout nextPutAll: '--------------------------------------'; nl.
    stdout nextPutAll: '1. Request a New Ride'; nl.
    stdout nextPutAll: '2. View My Ride History'; nl.
    stdout nextPutAll: '3. View All Drivers Logs'; nl.
    stdout nextPutAll: '4. Switch Active Rider Profile'; nl.
    stdout nextPutAll: '5. Save and Exit'; nl.
    stdout nextPutAll: 'Choose Option (1-5): '; flush.
    
    choice := stdin nextLine.
    choice ifNil: [ choice := '5' ].

    choice = '1' ifTrue: [

        | pickup dropoff distStr distance typeChoice rID ride driver cleanDistStr |
        pickup := getValidatedAddress value: 'Enter Pickup Address: '.
        dropoff := getValidatedAddress value: 'Enter Dropoff Address: '.
        
        stdout nextPutAll: 'Enter Distance (miles): '; flush.
        distStr := stdin nextLine.
        distStr ifNil: [ distStr := '1.0' ].
        
        "FIXED: Filters text symbols out from user inputs to avoid parsing execution crashes"
        cleanDistStr := distStr select: [ :char | char isDigit or: [ char = $. ] ].
        distance := cleanDistStr isEmpty ifTrue: [ 1.0 ] ifFalse: [ cleanDistStr asNumber asFloat ].
        
        stdout nextPutAll: 'Select Type (1 for Standard, 2 for Premium): '; flush.
        typeChoice := stdin nextLine.
        
        rID := 'R00', rideCounter printString.
        rideCounter := rideCounter + 1.
        
        typeChoice = '2' 
            ifTrue: [ ride := PremiumRide new initializeWithId: rID pickup: pickup dropoff: dropoff distance: distance ]
            ifFalse: [ ride := StandardRide new initializeWithId: rID pickup: pickup dropoff: dropoff distance: distance ].
            
        driver := selectRandomDriver value.
        activeRider requestRide: ride.
        driver addRide: ride.
        
        stdout nl; nextPutAll: '🎉 Match Finalized!'; nl.
        stdout nextPutAll: ('   Assigned Driver: ', driver getName); nl.
        stdout nextPutAll: '   '.
        ride rideDetails.
        stdout nl; flush.
    ].

    choice = '2' ifTrue: [
        stdout nl; nextPutAll: '--- YOUR PERSONAL HISTORY LOGS ---'; nl; flush.
        activeRider viewRides.
    ].

    choice = '3' ifTrue: [
        stdout nl; nextPutAll: '--- SYSTEM DRIVERS PERSISTED RECORDS ---'; nl; flush.
        globalDrivers do: [ :d |
            d getDriverInfo.
            stdout nextPutAll: '--------------------------------------'; nl; flush.
        ].
    ].

    choice = '4' ifTrue: [
        activeRider := handleRiderSession value.
        activeRider ifNil: [ activeRider := Rider new initializeWithId: 'U101' name: 'Guest' ].
    ].

    choice = '5' ifTrue: [
        storage saveStateRiders: globalRiders drivers: globalDrivers.
        stdout nl; nextPutAll: 'State saved securely to system_state.json. Goodbye!'; nl; flush.
    ].
].

ObjectMemory quit.
