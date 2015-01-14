import edu.wpi.first.wpilibj.networktables.NetworkTable;


public class NetworkTablesDesktopClient {
	
	/*
	 * Instance variables because I might have separate methods
	 * to turn the image into real motor values...It would be very
	 * messy if it was all in one method...Gross.
	 */
	private double leftMotorC = 0d;
	private double rightMotorC = 0d;
	
	public static void main(String[] args)
	{
		new NetworkTablesDesktopClient().run();
	}
	
	public void run()
	{
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-766.local");
		NetworkTable table = NetworkTable.getTable("dataTable");
		
		/*
		 * Done starts as true, so that if the image processing
		 * here does not work, or is not called, the commands during
		 * auto don't get hungup.
		 */
		boolean done = !(table.getBoolean("done"));
		
		while(true)
		{
			try{
				Thread.sleep(1000);
			}catch(InterruptedException ex)
			{
				System.out.println("0 noses.....I 2 tyd to swep");
			}
			
			
			
			while(!done)
			{
				//scan image and then set the motor values
			}
			
			
	    	table.putNumber("leftMotor", leftMotorC);
	    	table.putNumber("rightMotor", rightMotorC);
			System.out.println("Left: " + leftMotorC + " Right: " + rightMotorC);
		}
	}
}
