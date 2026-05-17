struct NetworkSession {
    session_id: String,
}

impl Drop for NetworkSession {
    fn drop(&mut self) {
        println!("[Rust] Connection Closed (Dropped): {}", self.session_id);
    }
}

fn main() {
    println!("Entering Scope...");
    // Allocated via Box (heap pointer)
    let s1 = Box::new(NetworkSession { session_id: String::from("Session_A") });
    
    println!("Exiting Scope...");
    // s1 goes out of scope here. Rust automatically drops it safely.
}