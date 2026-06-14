#ifndef DRIVER_H
#define DRIVER_H

#include <string>
#include <vector>
#include <memory>

class Ride; // Forward declaration fixes 'unidentified Ride' compilation errors

class Driver {
private:
    std::string driverID;     
    std::string name;          
    double rating;             
    std::vector<std::shared_ptr<Ride>> assignedRides; 

public:
    Driver(std::string id, std::string name, double rating);
    void addRide(std::shared_ptr<Ride> ride);
    void getDriverInfo() const;

    std::string getID() const { return driverID; }
    std::string getName() const { return name; }
    double getRating() const { return rating; }
};

#endif