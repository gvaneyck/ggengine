package LostCities

class Card implements Comparable<Card> {

    static gm
    static gs

    def value
    def color

    def Card(value, color) {
        this.value = value
        this.color = color
    }

    public String toString() {
    	return "${color} ${(value == 0 ? '!' : value)}"
	}

    int compareTo(Card o) {
        if (color.charAt(0) != o.color.charAt(0)) {
            return color.charAt(0) - o.color.charAt(0)
        }
        else {
            return value - o.value
        }
    }
}
