package com.aikido.musubi.domain.usecase
import android.graphics.PointF
import com.aikido.musubi.domain.model.*
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.*

class PoseAnalyzer {
    fun analyze(pose: Pose, type: ExerciseType): PostureFeedback = when (type) {
        ExerciseType.FINGER_CONNECTION -> analyzeFingerConnection(pose)
        ExerciseType.KINETIC_CHAIN -> analyzeKineticChain(pose)
        ExerciseType.BAMBOO_STICK -> analyzeBambooStick(pose)
        ExerciseType.SOLO_CENTER -> analyzeSoloCenter(pose)
    }

    private fun analyzeFingerConnection(pose: Pose): PostureFeedback {
        val issues = mutableListOf<String>(); val pos = mutableListOf<String>(); var score = 1f
        val ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: return noData(ExerciseType.FINGER_CONNECTION)
        val rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) ?: return noData(ExerciseType.FINGER_CONNECTION)
        val lw = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: return noData(ExerciseType.FINGER_CONNECTION)
        val rw = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST) ?: return noData(ExerciseType.FINGER_CONNECTION)
        val le = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW) ?: return noData(ExerciseType.FINGER_CONNECTION)
        val re = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW) ?: return noData(ExerciseType.FINGER_CONNECTION)
        val leftExt = lw.position.x > ls.position.x + 50
        val rightExt = rw.position.x < rs.position.x - 50
        if (!leftExt && !rightExt) { issues.add("Extend one arm forward at shoulder height"); score -= 0.25f } else pos.add("Arm extended well")
        val aw = if (leftExt) lw else rw; val as_ = if (leftExt) ls else rs
        if (abs(aw.position.y - as_.position.y) > 60) { issues.add("Raise/lower wrist to shoulder height"); score -= 0.20f } else pos.add("Wrist at correct height")
        if (abs(ls.position.y - rs.position.y) > 40) { issues.add("Keep shoulders level"); score -= 0.15f } else pos.add("Shoulders level")
        val ea = if (leftExt) angle(ls.position, le.position, lw.position) else angle(rs.position, re.position, rw.position)
        if (ea < 140f) { issues.add("Extend arm more — elbow too bent"); score -= 0.15f } else pos.add("Good elbow extension")
        return PostureFeedback(score.coerceIn(0f,1f), score >= 0.65f, issues, pos, ExerciseType.FINGER_CONNECTION)
    }

    private fun analyzeKineticChain(pose: Pose): PostureFeedback {
        val issues = mutableListOf<String>(); val pos = mutableListOf<String>(); var score = 1f
        val ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: return noData(ExerciseType.KINETIC_CHAIN)
        val rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) ?: return noData(ExerciseType.KINETIC_CHAIN)
        val lh = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) ?: return noData(ExerciseType.KINETIC_CHAIN)
        val rh = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) ?: return noData(ExerciseType.KINETIC_CHAIN)
        val la = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val ra = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        if (abs((ls.position.x+rs.position.x)/2 - (lh.position.x+rh.position.x)/2) > 50) { issues.add("Stand more upright — you're leaning"); score -= 0.20f } else pos.add("Good upright posture")
        if (abs(lh.position.y - rh.position.y) > 35) { issues.add("Keep hips level"); score -= 0.15f } else pos.add("Hips neutral and level")
        val sw = abs(ls.position.x - rs.position.x); val hw = abs(lh.position.x - rh.position.x)
        val r = if (hw > 0) sw/hw else 1f
        if (r < 0.8f || r > 1.8f) { issues.add("Keep feet shoulder-width apart"); score -= 0.15f } else pos.add("Stable stance")
        if (la != null && ra != null && abs(la.position.y - ra.position.y) > 40) { issues.add("Balance weight evenly"); score -= 0.10f } else pos.add("Weight evenly distributed")
        return PostureFeedback(score.coerceIn(0f,1f), score >= 0.65f, issues, pos, ExerciseType.KINETIC_CHAIN)
    }

    private fun analyzeBambooStick(pose: Pose): PostureFeedback {
        val issues = mutableListOf<String>(); val pos = mutableListOf<String>(); var score = 1f
        val ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: return noData(ExerciseType.BAMBOO_STICK)
        val rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) ?: return noData(ExerciseType.BAMBOO_STICK)
        val lw = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: return noData(ExerciseType.BAMBOO_STICK)
        val rw = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST) ?: return noData(ExerciseType.BAMBOO_STICK)
        val le = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW) ?: return noData(ExerciseType.BAMBOO_STICK)
        val re = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW) ?: return noData(ExerciseType.BAMBOO_STICK)
        if (abs(lw.position.y - rw.position.y) > 50) { issues.add("Keep both hands at same height"); score -= 0.25f } else pos.add("Hands at even height")
        if (abs((lw.position.y+rw.position.y)/2 - (ls.position.y+rs.position.y)/2) > 70) { issues.add("Raise hands to shoulder height"); score -= 0.20f } else pos.add("Arms at correct height")
        if (angle(ls.position,le.position,lw.position) > 175f || angle(rs.position,re.position,rw.position) > 175f) { issues.add("Soften the elbow — don't lock arms"); score -= 0.15f } else pos.add("Soft elbow bend")
        if (abs(ls.position.y - rs.position.y) > 35) { issues.add("Level the shoulders"); score -= 0.15f } else pos.add("Shoulders balanced")
        return PostureFeedback(score.coerceIn(0f,1f), score >= 0.65f, issues, pos, ExerciseType.BAMBOO_STICK)
    }

    private fun analyzeSoloCenter(pose: Pose): PostureFeedback {
        val issues = mutableListOf<String>(); val pos = mutableListOf<String>(); var score = 1f
        val ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: return noData(ExerciseType.SOLO_CENTER)
        val rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) ?: return noData(ExerciseType.SOLO_CENTER)
        val lh = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) ?: return noData(ExerciseType.SOLO_CENTER)
        val rh = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) ?: return noData(ExerciseType.SOLO_CENTER)
        val le = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
        val re = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
        if (le != null && re != null && abs(le.position.y - re.position.y) > 20) { issues.add("Level your head — chin parallel to floor"); score -= 0.15f } else pos.add("Head level")
        if (abs((ls.position.x+rs.position.x)/2 - (lh.position.x+rh.position.x)/2) > 40) { issues.add("Align spine — shoulders over hips"); score -= 0.20f } else pos.add("Spine aligned")
        val tl = abs((ls.position.y+rs.position.y)/2 - (lh.position.y+rh.position.y)/2)
        val sw = abs(ls.position.x - rs.position.x)
        if (sw > 0 && tl/sw < 0.8f) { issues.add("Roll shoulders back and stand taller"); score -= 0.20f } else pos.add("Shoulders back, posture open")
        if (abs(ls.position.y - rs.position.y) > 30) { issues.add("Relax and level the shoulders"); score -= 0.15f } else pos.add("Shoulders relaxed and level")
        return PostureFeedback(score.coerceIn(0f,1f), score >= 0.65f, issues, pos, ExerciseType.SOLO_CENTER)
    }

    private fun noData(type: ExerciseType) = PostureFeedback(0.5f, false, listOf("Move into frame fully"), emptyList(), type)

    private fun angle(a: PointF, b: PointF, c: PointF): Float {
        var deg = Math.toDegrees(atan2((c.y-b.y).toDouble(),(c.x-b.x).toDouble()) - atan2((a.y-b.y).toDouble(),(a.x-b.x).toDouble())).toFloat()
        if (deg < 0) deg += 360f; if (deg > 180) deg = 360f - deg; return deg
    }
}