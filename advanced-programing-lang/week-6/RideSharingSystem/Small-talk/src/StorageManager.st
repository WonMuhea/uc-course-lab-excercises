"========================================================================
 @class StorageManager
 @brief Flat file JSON string tokenizer matching the C++ parsing system
 ========================================================================"

Object subclass: StorageManager [

    | filename |

    <category: 'RideSharingSystem'>

    initializeWithFile: file [
        filename := file.
    ]

    "Generic safe float formatter for storage"
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

    "FIXED: Safe custom text filtering block that removes JSON syntax garbage cleanly without error-prone regex offsets"
    extractJSONValue: line key: key [

        | searchStr keyIndex startPos subLine cleanLine |
        searchStr := '"', key, '"'.
        keyIndex := line indexOfSubCollection: searchStr startingAt: 1.
        keyIndex = 0 ifTrue: [ ^ '' ].
        
        "Find the starting position past the separator colon"
        startPos := line indexOfSubCollection: ':' startingAt: keyIndex.
        startPos = 0 ifTrue: [ ^ '' ].
        startPos := startPos + 1.
        
        "Isolate remaining line data chunks"
        subLine := line copyFrom: startPos to: line size.
        
        "Filter down characters to completely strip structural punctuation and whitespaces out of matching elements"
        cleanLine := subLine select: [ :char |
            (char = $" or: [ char = $, or: [ char = $} or: [ char = ${ or: [ char isSeparator ] ] ] ]) not
        ].
        
        ^ cleanLine.
    ]

    saveStateRiders: riders drivers: drivers [

        | stream |
        stream := FileStream open: filename mode: FileStream write.
        [
            stream nextPutAll: '{'; nl.
            stream nextPutAll: '  "riders": ['; nl.
            riders doWithIndex: [ :rider :idx |
                stream nextPutAll: ('    { "id": "', rider getID, '", "name": "', rider getName, '" }').
                idx < riders size ifTrue: [ stream nextPutAll: ',' ].
                stream nl.
            ].
            stream nextPutAll: '  ],'; nl.
            stream nextPutAll: '  "drivers": ['; nl.
            drivers doWithIndex: [ :driver :idx |

                | safeRating |
                safeRating := self formatFloat: driver getRating precision: 1.
                stream nextPutAll: ('    { "id": "', driver getID, '", "name": "', driver getName, '", "rating": ', safeRating, ' }').
                idx < drivers size ifTrue: [ stream nextPutAll: ',' ].
                stream nl.
            ].
            stream nextPutAll: '  ]'; nl.
            stream nextPutAll: '}'.
        ] ensure: [ stream close ].
    ]

    loadStateIntoRiders: riders intoDrivers: drivers [

        | file line parsingRiders parsingDrivers |
        (File exists: filename) ifFalse: [
            drivers add: (Driver new initializeWithId: 'D01' name: 'Carlos (Tesla Model 3)' rating: 4.9).
            drivers add: (Driver new initializeWithId: 'D02' name: 'Diana (Toyota Prius)' rating: 4.7).
            drivers add: (Driver new initializeWithId: 'D03' name: 'Evan (BMW M4)' rating: 5.0).
            ^ self.
        ].

        riders empty.
        drivers empty.
        file := FileStream open: filename mode: FileStream read.
        parsingRiders := false.
        parsingDrivers := false.
        
        [ file atEnd ] whileFalse: [
            line := file nextLine.
            (line matchRegex: '.*"riders".*') ifTrue: [ parsingRiders := true. parsingDrivers := false ].
            (line matchRegex: '.*"drivers".*') ifTrue: [ parsingDrivers := true. parsingRiders := false ].
            
            (line matchRegex: '.*\{.*') ifTrue: [
                parsingRiders ifTrue: [

                    | id name |
                    id := self extractJSONValue: line key: 'id'.
                    name := self extractJSONValue: line key: 'name'.
                    (id isEmpty or: [ name isEmpty ]) ifFalse: [
                        riders add: (Rider new initializeWithId: id name: name).
                    ].
                ].
                parsingDrivers ifTrue: [

                    | id name ratingStr cleanRatingStr rating |
                    id := self extractJSONValue: line key: 'id'.
                    name := self extractJSONValue: line key: 'name'.
                    ratingStr := self extractJSONValue: line key: 'rating'.
                    
                    (id isEmpty or: [ name isEmpty ]) ifFalse: [
                        cleanRatingStr := ratingStr select: [ :char | char isDigit or: [ char = $. ] ].
                        
                        "FIXED: Replaced #asFloat with native GNU Smalltalk #asNumber conversion"
                        rating := cleanRatingStr isEmpty ifTrue: [ 5.0 ] ifFalse: [ cleanRatingStr asNumber ].
                        drivers add: (Driver new initializeWithId: id name: name rating: rating).
                    ].
                ].
            ].
        ].
        file close.
    ]
]
