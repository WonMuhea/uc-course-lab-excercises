#include <iostream>
#include <vector>
#include <memory>
#include <iomanip>
#include <string>
#include <regex>
#include <random>
#include "StandardRide.h"
#include "PremiumRide.h"
#include "Driver.h"
#include "Rider.h"
#include "StorageManager.h"

bool validateAddress(const std::string& address) {
    std::regex addressPattern("^(.+),\\s*([A-Z]{2})\\s+(\\d{5})$");
    return std::regex_match(address, addressPattern);
}

std::string getValidatedAddress(const std::string& prompt) {
    std::string address;
    while (true) {
        std::cout << prompt;
        std::getline(std::cin, address);
        if (validateAddress(address)) return address;
        std::cout << "❌ Invalid! Format: [Street Name], [State] [Zipcode] (e.g., 123 Main St, VA 22314)\n\n";
    }
}

std::vector<std::shared_ptr<Rider>> globalRiders;
std::vector<std::shared_ptr<Driver>> globalDrivers;

std::shared_ptr<Rider> handleRiderSession(StorageManager& storage) {
    std::string inputName;
    std::cout << "=== RIDER AUTHENTICATION MODULE ===\n";
    std::cout << "Enter your Rider Name: ";
    std::getline(std::cin, inputName);

    for (auto& r : globalRiders) {
        if (r->getName() == inputName) {
            std::cout << "✅ Welcome back, " << inputName << "!\n\n";
            return r;
        }
    }

    std::string nextID = "U" + std::to_string(globalRiders.size() + 101);
    auto newRider = std::make_shared<Rider>(nextID, inputName);
    globalRiders.push_back(newRider);
    storage.saveState(globalRiders, globalDrivers);
    std::cout << "👤 New rider profile created for " << inputName << " (ID: " << nextID << ").\n\n";
    return newRider;
}

std::shared_ptr<Driver> selectRandomDriver() {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> distr(0, globalDrivers.size() - 1);
    return globalDrivers[distr(gen)];
}

int main() {
    std::cout << std::fixed << std::setprecision(2);
    StorageManager storage("system_state.json");
    storage.loadState(globalRiders, globalDrivers);

    std::shared_ptr<Rider> activeRider = handleRiderSession(storage);
    int rideCounter = 1;
    int choice = 0;

    while (choice != 5) {
        std::cout << "======================================\n";
        std::cout << "    RIDE-SHARING PERSISTENT SYSTEM    \n";
        std::cout << "======================================\n";
        std::cout << "Logged in as: " << activeRider->getName() << "\n";
        std::cout << "--------------------------------------\n";
        std::cout << "1. Request a New Ride\n";
        std::cout << "2. View My Ride History\n";
        std::cout << "3. View All Drivers Logs\n";
        std::cout << "4. Switch Active Rider Profile\n";
        std::cout << "5. Save and Exit\n";
        std::cout << "Choose Option (1-5): ";
        
        if (!(std::cin >> choice)) {
            std::cout << "Invalid numeric option expected.\n";
            std::cin.clear();
            std::cin.ignore(10000, '\n');
            continue;
        }
        std::cin.ignore();

        switch (choice) {
            case 1: {
                std::string pickup = getValidatedAddress("Enter Pickup Address: ");
                std::string dropoff = getValidatedAddress("Enter Dropoff Address: ");
                double distance;
                int typeChoice;

                std::cout << "Enter Distance (miles): ";
                while (!(std::cin >> distance) || distance <= 0) {
                    std::cout << "Invalid. Enter positive distance value: ";
                    std::cin.clear();
                    std::cin.ignore(10000, '\n');
                }

                std::cout << "Select Type (1 for Standard, 2 for Premium): ";
                while (!(std::cin >> typeChoice) || (typeChoice != 1 && typeChoice != 2)) {
                    std::cout << "Enter 1 or 2: ";
                    std::cin.clear();
                    std::cin.ignore(10000, '\n');
                }

                std::string rID = "R00" + std::to_string(rideCounter++);
                std::shared_ptr<Ride> ride = nullptr;

                if (typeChoice == 1) {
                    ride = std::make_shared<StandardRide>(rID, pickup, dropoff, distance);
                } else {
                    ride = std::make_shared<PremiumRide>(rID, pickup, dropoff, distance);
                }
            

                std::shared_ptr<Driver> driver = selectRandomDriver();
                activeRider->requestRide(ride);
                driver->addRide(ride);

                std::cout << "\n🎉 Match Finalized!\n   Assigned Driver: " << driver->getName() << "\n   ";
                ride->rideDetails();
                std::cout << "\n";
                break;
            }
            case 2:
                std::cout << "\n--- YOUR PERSONAL HISTORY LOGS ---\n";
                activeRider->viewRides();
                break;
            case 3:
                std::cout << "\n--- SYSTEM DRIVERS PERSISTED RECORDS ---\n";
                for (const auto& d : globalDrivers) {
                    d->getDriverInfo();
                    std::cout << "--------------------------------------\n";
                }
                break;
            case 4:
                activeRider = handleRiderSession(storage);
                break;
            case 5:
                storage.saveState(globalRiders, globalDrivers);
                std::cout << "\nState saved securely to system_state.json. Goodbye!\n";
                break;
            default:
                std::cout << "Invalid selection range.\n";
        }
    }
    return 0;
}