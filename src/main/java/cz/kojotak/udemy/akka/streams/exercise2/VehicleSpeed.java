package cz.kojotak.udemy.akka.streams.exercise2;

public class VehicleSpeed {
    private int vehicleId;
    private double speed;

    public VehicleSpeed(int vehicleId, double speed) {
        this.vehicleId = vehicleId;
        this.speed= speed;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public double getSpeed() {
        return speed;
    }

	@Override
	public String toString() {
		return "VehicleSpeed [vehicleId=" + vehicleId + ", speed=" + speed + "]";
	}
    
}
