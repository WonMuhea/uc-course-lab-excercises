#ifndef RIDER_H
#define RIDER_H

#include <string>
#include <vector>
#include <memory>

class Ride; // Forward declaration fixes 'unidentified Ride' compilation errors

class Rider {
private:
    std::string riderID;       
    std::string name;          
    std::vector<std::shared_ptr<Ride>> requestedRides;

public:
    Rider(std::string id, std::string name);
    void requestRide(std::shared_ptr<Ride> ride);
    void viewRides() const;

    std::string getID() const { return riderID; }
    std::string getName() const { return name; }
};

#endif