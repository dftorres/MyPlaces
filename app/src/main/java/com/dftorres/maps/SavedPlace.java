package com.dftorres.maps;

public class SavedPlace {
    private String placeName;
    private String placeLat;
    private String placeLng;
    private String placeAddress;
    private byte[] placeImage;

    /**
     * Crea un objeto SavedPlace para almacenar los lugares guardados
     */
    public SavedPlace(String placeName, String placeLat, String placeLng, String placeAddress, byte[] placeImage) {
        setPlaceName(placeName);
        setPlaceLat(placeLat);
        setPlaceLng(placeLng);
        setPlaceAddress(placeAddress);
        setPlaceImage(placeImage);
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceLat() {
        return placeLat;
    }

    public void setPlaceLat(String placeLat) {
        this.placeLat = placeLat;
    }

    public String getPlaceLng() {
        return placeLng;
    }

    public void setPlaceLng(String placeLng) {
        this.placeLng = placeLng;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public byte[] getPlaceImage() {
        return placeImage;
    }

    public void setPlaceImage(byte[] placeImage) {
        this.placeImage = placeImage;
    }
}
