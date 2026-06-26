from collections import Counter

class StatisticsCalculator:
    """
    Class to encapsulate statistical operations on a dataset.
    """
    def __init__(self, data):
        # Data is stored as an instance attribute
        self.data = sorted(data)
        
    def get_mean(self):
        # Encapsulated logic for arithmetic mean
        return sum(self.data) / len(self.data)
    
    def get_median(self):
        # Encapsulated logic for median based on object data
        n = len(self.data)
        mid = n // 2
        if n % 2 == 0:
            return (self.data[mid - 1] + self.data[mid]) / 2
        return float(self.data[mid])
    
    def get_mode(self):
        # Uses Counter for clean mapping of frequency
        counts = Counter(self.data)
        max_freq = max(counts.values())
        return [val for val, count in counts.items() if count == max_freq]

# Usage instance
if __name__ == "__main__":
    calc = StatisticsCalculator([3, 1, 4, 1, 5, 9, 2, 6, 5, 5])
    print(f"Mean: {calc.get_mean()}")
    print(f"Median: {calc.get_median()}")
    print(f"Modes: {calc.get_mode()}")