package hotel.entities;


import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;

@RunWith(JUnitPlatform.class)

@SelectClasses({TestRoom.class})
//@SelectPackages("hotel.entities")

class TestAll {



}
