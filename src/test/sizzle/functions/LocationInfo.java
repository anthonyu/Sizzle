package sizzle.functions;

import sizzle.functions.FunctionSpec;
import sizzle.types.SizzleLocation.Location;

import com.google.protobuf.InvalidProtocolBufferException;

public class LocationInfo {
	@FunctionSpec(name = "locationinfo", returnType = "Location", formalParameters = { "string" }, typeDependencies = { "sizzle_location.proto" })
	public static Location locationInfo(final String location) {
		try {
			return Location.parseFrom(location.getBytes());
		} catch (final InvalidProtocolBufferException e) {
			return null;
		}
	}
}
