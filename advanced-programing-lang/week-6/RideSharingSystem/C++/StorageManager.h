#ifndef STORAGEMANAGER_H
#define STORAGEMANAGER_H

#include <string>
#include <vector>
#include <memory>
#include "Rider.h"
#include "Driver.h"

class StorageManager {
private:
    std::string filename;
    std::string extractJSONValue(const std::string& jsonRecord, const std::string& key);

public:
    explicit StorageManager(std::string file) : filename(std::move(file)) {}
    void saveState(const std::vector<std::shared_ptr<Rider>>& riders, const std::vector<std::shared_ptr<Driver>>& drivers);
    void loadState(std::vector<std::shared_ptr<Rider>>& riders, std::vector<std::shared_ptr<Driver>>& drivers);
};

#endif