(* Calculates the arithmetic mean using List.fold_left to sum the list *)
let mean list = 
  let sum = List.fold_left (fun acc x -> acc + x) 0 list in
  float_of_int sum /. float_of_int (List.length list)

(* Calculates the median by sorting and using pattern matching on list length *)
let median list =
  let sorted = List.sort compare list in
  let len = List.length sorted in
  let mid = len / 2 in
  if len mod 2 = 0 then
    float_of_int (List.nth sorted (mid - 1) + List.nth sorted mid) /. 2.0
  else
    float_of_int (List.nth sorted mid)

(* Calculates the mode using high-order list operations to count occurrences *)
let mode list =
  (* Build an association list of (value, frequency) pairs *)
  let counts = List.fold_left (fun acc x -> 
    let current = try List.assoc x acc with Not_found -> 0 in
    (x, current + 1) :: List.remove_assoc x acc) [] list in
  
  (* Find the maximum frequency value *)
  let max_freq = List.fold_left (fun acc (_, count) -> max acc count) 0 counts in
  
  (* Filter values that appear with the max frequency *)
  List.filter (fun (_, count) -> count = max_freq) counts 
  |> List.map fst

let () =
  let data = [3; 1; 4; 1; 5; 9; 2; 6; 5; 5] in
  Printf.printf "Mean: %.2f\n" (mean data);
  Printf.printf "Median: %.2f\n" (median data);
  print_endline ("Modes: " ^ String.concat " " (List.map string_of_int (mode data)))