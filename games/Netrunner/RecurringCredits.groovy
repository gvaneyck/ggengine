package Netrunner

class RecurringCredits {
    static gs

    def remaining
    def max
    def useCredits = true
    def stealth = false
    def useCase

    def RecurringCredits(max, useCase, parent) {
        this.max = max
        this.remaining = max
        this.useCase = useCase
        gs.events << [ name: 'onTrash', target: parent, event: { gs.runner.recurring.remove(this) }]
    }

    def creditsAvailable(context) {
        if (!useCredits || remaining == 0) {
            return 0
        }
        if (useCase(context)) {
            return remaining
        }
        return 0
    }
}
