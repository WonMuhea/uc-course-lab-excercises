public class NetworkSession {
    public static void main(String[] args) {
        
        NetworkSession s1 = new NetworkSession("Session_A");
        s1 = null; 
        System.gc();
       
        try { Thread.sleep(100); } catch (InterruptedException e) {}
    }

    class NetworkSession {
        String sessionId;
        NetworkSession(String id) {
            this.sessionId = id;
            System.out.println("[Java] Connection Opened: " + this.sessionId);
        }

        @SuppressWarnings("removal")
        @Override
        protected void finalize() {
            System.out.println("[Java] GC automatically closed: " + this.sessionId);
        }
}
}