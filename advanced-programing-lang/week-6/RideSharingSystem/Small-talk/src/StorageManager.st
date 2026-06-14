"========================================================================
 @class StorageManager
 @brief Flat file string tokenizer matching the C++ parsing system
 ========================================================================"

Object subclass: StorageManager [

    | filename |

    <category: 'RideSharingSystem'>

    initializeWithFile: file [
        filename := file.
    ]

    "BYPASS METHOD: Manually formats a float to string using safe integer math to prevent ZeroDivide crashes"
    safeStringFromFloat: aFloat [

        | wholePart fractionPart totalCents |
        
        "Convert the float to a precise number of cents (e.g., 4.9 -> 49)"
        totalCents := (aFloat * 10) rounded.
        
        "Extract the whole number part via integer division"
        wholePart := totalCents // 10.
        
        "Extract the remainder tenths digit via modulo"
        fractionPart := totalCents \\ 10.
        
        ^ wholePart printString, '.', fractionPart printString.
    ]

    extractJSONValue: line key: key [

        | keyPattern matchResults startPos remainder endPos delimiters |
        keyPattern := '"', key, '"\s*:\s*"?'.
        matchResults := line searchRegex: keyPattern.
        (matchResults isNil or: [ matchResults isEmpty ]) ifTrue: [ ^ '' ].

        startPos := (matchResults at: 1) last.
        remainder := line copyFrom: (startPos + 1) to: line size.
        
        delimiters := Set withAll: { $". $,. $}. Character space. Character tab. Character cr. Character lf }.
        endPos := remainder findFirst: [ :char | delimiters includes: char ].
        endPos = 0 ifTrue: [ ^ remainder ].
        
        ^ remainder copyFrom: 1 to: (endPos - 1).
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
                "FIXED: Invokes our manual calculation block instead of the broken native engine"
                safeRating := self safeStringFromFloat: driver getRating.
                
                stream nextPutAll: ('    { "id": "', driver getID, '", "name": "', driver getName, '", "rating": ', safeRating, ' }').
                idx < drivers size ifTrue: [ stream nextPutAll: ',' ].
                stream nl.
            ].
            stream nextPutAll: '  ]'; nl.
            stream nextPutAll: '}'.
        ] ensure: [ stream close ].
    ]

    loadStateIntoRiders: riders intoDrivers: drivers [

        | file line parsingRiders parsingDrivers id name ratingStr rating |
        (File exists: filename) ifFalse: [
            drivers add: (Driver new initializeWithId: 'D01' name: 'Carlos (Tesla Model 3)' rating: 4.9).
            drivers add: (Driver new initializeWithId: 'D02' name: 'Diana (Toyota Prius)' rating: 4.7).
            drivers add: (Driver new initializeWithId: 'D03' name: 'Evan (BMW M4)' rating: 5.0).
            ^ self.
        ].

        "GNU Smalltalk compatibility: replace removeAll with manual clearing"
        [ riders isEmpty ] whileFalse: [ riders removeLast ].
        [ drivers isEmpty ] whileFalse: [ drivers removeLast ].

        file := FileStream open: filename mode: FileStream read.
        parsingRiders := false.
        parsingDrivers := false.
        
        [ file atEnd ] whileFalse: [
            line := file nextLine.
            (line includesString: '"riders"') ifTrue: [ parsingRiders := true. parsingDrivers := false ].
            (line includesString: '"drivers"') ifTrue: [ parsingDrivers := true. parsingRiders := false ].
            
            (line includesString: '{') ifTrue: [
                parsingRiders ifTrue: [
                    id := self extractJSONValue: line key: 'id'.
                    name := self extractJSONValue: line key: 'name'.
                    (id isEmpty or: [ name isEmpty ]) ifFalse: [
                        riders add: (Rider new initializeWithId: id name: name).
                    ].
                ].
                parsingDrivers ifTrue: [
                    id := self extractJSONValue: line key: 'id'.
                    name := self extractJSONValue: line key: 'name'.
                    ratingStr := self extractJSONValue: line key: 'rating'.
                    (id isEmpty or: [ name isEmpty ]) ifFalse: [
                        rating := ratingStr isEmpty ifTrue: [ 5.0 ] ifFalse: [ ratingStr asNumber asFloat ].
                        drivers add: (Driver new initializeWithId: id name: name rating: rating).
                    ].
                ].
            ].
        ].
        file close.
    ]
]
