#include "StorageManager.h"
#include <fstream>
#include <sstream>
#include <iostream>

std::string StorageManager::extractJSONValue(const std::string& line, const std::string& key) {
    size_t keyPos = line.find("\"" + key + "\"");
    if (keyPos == std::string::npos) return "";
    size_t colonPos = line.find(":", keyPos);
    if (colonPos == std::string::npos) return "";
    size_t startPos = line.find_first_not_of(" \t\"", colonPos + 1);
    size_t endPos = line.find_first_of("\",}", startPos);
    if (startPos != std::string::npos && endPos != std::string::npos) {
        return line.substr(startPos, endPos - startPos);
    }
    return "";
}

void StorageManager::saveState(const std::vector<std::shared_ptr<Rider>>& riders, const std::vector<std::shared_ptr<Driver>>& drivers) {
    std::ofstream outFile(filename);
    if (!outFile.is_open()) return;

    outFile << "{\n  \"riders\": [\n";
    for (size_t i = 0; i < riders.size(); ++i) {
        outFile << "    { \"id\": \"" << riders[i]->getID() << "\", \"name\": \"" << riders[i]->getName() << "\" }";
        if (i < riders.size() - 1) outFile << ",";
        outFile << "\n";
    }
    outFile << "  ],\n  \"drivers\": [\n";
    for (size_t i = 0; i < drivers.size(); ++i) {
        outFile << "    { \"id\": \"" << drivers[i]->getID() << "\", \"name\": \"" << drivers[i]->getName() << "\", \"rating\": " << drivers[i]->getRating() << " }";
        if (i < drivers.size() - 1) outFile << ",";
        outFile << "\n";
    }
    outFile << "  ]\n}";
    outFile.close();
}

void StorageManager::loadState(std::vector<std::shared_ptr<Rider>>& riders, std::vector<std::shared_ptr<Driver>>& drivers) {
    std::ifstream inFile(filename);
    if (!inFile.is_open()) {
        drivers.push_back(std::make_shared<Driver>("D01", "Carlos (Tesla Model 3)", 4.9));
        drivers.push_back(std::make_shared<Driver>("D02", "Diana (Toyota Prius)", 4.7));
        drivers.push_back(std::make_shared<Driver>("D03", "Evan (BMW M4)", 5.0));
        return;
    }

    riders.clear();
    drivers.clear();
    std::string line;
    bool parsingRiders = false;
    bool parsingDrivers = false;

    while (std::getline(inFile, line)) {
        if (line.find("\"riders\"") != std::string::npos) { parsingRiders = true; parsingDrivers = false; continue; }
        if (line.find("\"drivers\"") != std::string::npos) { parsingDrivers = true; parsingRiders = false; continue; }
        
        if (parsingRiders && line.find("{") != std::string::npos) {
            std::string id = extractJSONValue(line, "id");
            std::string name = extractJSONValue(line, "name");
            if (!id.empty() && !name.empty()) riders.push_back(std::make_shared<Rider>(id, name));
        }
        if (parsingDrivers && line.find("{") != std::string::npos) {
            std::string id = extractJSONValue(line, "id");
            std::string name = extractJSONValue(line, "name");
            std::string ratingStr = extractJSONValue(line, "rating");
            if (!id.empty() && !name.empty()) {
                double rating = ratingStr.empty() ? 5.0 : std::stod(ratingStr);
                drivers.push_back(std::make_shared<Driver>(id, name, rating));
            }
        }
    }
    inFile.close();
}