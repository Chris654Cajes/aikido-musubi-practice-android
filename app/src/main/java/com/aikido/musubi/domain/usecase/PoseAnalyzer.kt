package com.aikido.musubi.domain.usecase

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.aikido.musubi.domain.model.ExerciseType
import com.aikido.musubi.domain.model.PostureFeedback
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class PoseAnalyzer {

    fun analyze(pose: Pose, exerciseType: ExerciseType): PostureFeedback {
        return when (exerciseType) {
            ExerciseType.FINGER_CONNECTION -> analyzeFingerConnection(pose)
            ExerciseType.KINETIC_CHAIN     -> analyzeKineticChain(pose)
            ExerciseType.BAMBOO_STICK      -> analyzeBambooStick(pose)
            ExerciseType.SOLO_CENTER       -> analyzeSoloCenter(pose)
        }
    }

    // ── FINGER CONNECTION ─────────────────────────────────────────────────────
    // Checks: arm extended forward at shoulder height, wrist relaxed, shoulder level
    private fun analyzeFingerConnection(pose: Pose): PostureFeedback {
        val issues   = mutableListOf<String>()
        val positive = mutableListOf<String>()
        var score    = 1.0f

        val leftShoulder  = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftWrist     = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist    = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftElbow     = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow    = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

        if (leftShoulder == null || rightShoulder == null ||
            leftWrist == null || rightWrist == null ||
            leftElbow == null || rightElbow == null) {
            return PostureFeedback(0.5f, false, listOf("Move into frame fully"), emptyList(), ExerciseType.FINGER_CONNECTION)
        }

        // Check: at least one arm should be extended (wrist ahead of shoulder in x or z)
        val leftArmExtended  = leftWrist.position.x > leftShoulder.position.x + 50
        val rightArmExtended = rightWrist.position.x < rightShoulder.position.x - 50

        if (!leftArmExtended && !rightArmExtended) {
            issues.add("Extend one arm forward at shoulder height")
            score -= 0.25f
        } else {
            positive.add("Arm extended well")
        }

        // Check: wrist height relative to shoulder (within ±30px)
        val activeWrist    = if (leftArmExtended) leftWrist else rightWrist
        val activeShoulder = if (leftArmExtended) leftShoulder else rightShoulder
        val wristHeightDiff = abs(activeWrist.position.y - activeShoulder.position.y)

        if (wristHeightDiff > 60) {
            issues.add("Raise/lower wrist to shoulder height")
            score -= 0.20f
        } else {
            positive.add("Wrist at correct shoulder height")
        }

        // Check: shoulder not raised (both shoulders roughly level)
        val shoulderLevelDiff = abs(leftShoulder.position.y - rightShoulder.position.y)
        if (shoulderLevelDiff > 40) {
            issues.add("Keep shoulders level — drop the raised shoulder")
            score -= 0.15f
        } else {
            positive.add("Shoulders level and relaxed")
        }

        // Check: elbow angle (should not be fully locked — ideal ~150–170°)
        val elbowAngle = if (leftArmExtended) {
            calculateAngle(leftShoulder.position, leftElbow.position, leftWrist.position)
        } else {
            calculateAngle(rightShoulder.position, rightElbow.position, rightWrist.position)
        }
        if (elbowAngle < 140f) {
            issues.add("Extend arm more — elbow too bent")
            score -= 0.15f
        } else if (elbowAngle > 175f) {
            positive.add("Elbow softly extended")
        } else {
            positive.add("Good elbow extension")
        }

        score = score.coerceIn(0f, 1f)
        return PostureFeedback(score, score >= 0.65f, issues, positive, ExerciseType.FINGER_CONNECTION)
    }

    // ── KINETIC CHAIN ─────────────────────────────────────────────────────────
    // Checks: upright stance, feet shoulder-width, spine erect, hips neutral
    private fun analyzeKineticChain(pose: Pose): PostureFeedback {
        val issues   = mutableListOf<String>()
        val positive = mutableListOf<String>()
        var score    = 1.0f

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder= pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftHip      = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip     = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftAnkle    = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle   = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val nose         = pose.getPoseLandmark(PoseLandmark.NOSE)

        if (leftShoulder == null || rightShoulder == null ||
            leftHip == null || rightHip == null) {
            return PostureFeedback(0.5f, false, listOf("Step back — full body must be visible"), emptyList(), ExerciseType.KINETIC_CHAIN)
        }

        // Check: upright posture (shoulder center above hip center vertically)
        val shoulderCenterX = (leftShoulder.position.x + rightShoulder.position.x) / 2
        val hipCenterX      = (leftHip.position.x + rightHip.position.x) / 2
        val forwardLean     = abs(shoulderCenterX - hipCenterX)

        if (forwardLean > 50) {
            issues.add("Stand more upright — you're leaning sideways")
            score -= 0.20f
        } else {
            positive.add("Good upright posture")
        }

        // Check: hips level
        val hipLevelDiff = abs(leftHip.position.y - rightHip.position.y)
        if (hipLevelDiff > 35) {
            issues.add("Keep hips level — avoid tilting")
            score -= 0.15f
        } else {
            positive.add("Hips are neutral and level")
        }

        // Check: shoulder width vs hip width (should be similar for natural stance)
        val shoulderWidth = abs(leftShoulder.position.x - rightShoulder.position.x)
        val hipWidth      = abs(leftHip.position.x - rightHip.position.x)
        val widthRatio    = if (hipWidth > 0) shoulderWidth / hipWidth else 1f

        if (widthRatio < 0.8f || widthRatio > 1.8f) {
            issues.add("Keep feet shoulder-width apart for stability")
            score -= 0.15f
        } else {
            positive.add("Stable shoulder-width stance")
        }

        // Check: feet visible and roughly level (if ankles detected)
        if (leftAnkle != null && rightAnkle != null) {
            val ankleDiff = abs(leftAnkle.position.y - rightAnkle.position.y)
            if (ankleDiff > 40) {
                issues.add("Balance weight evenly on both feet")
                score -= 0.10f
            } else {
                positive.add("Weight evenly distributed")
            }
        }

        score = score.coerceIn(0f, 1f)
        return PostureFeedback(score, score >= 0.65f, issues, positive, ExerciseType.KINETIC_CHAIN)
    }

    // ── BAMBOO STICK ──────────────────────────────────────────────────────────
    // Checks: both arms extended forward, level shoulder line, forward stance
    private fun analyzeBambooStick(pose: Pose): PostureFeedback {
        val issues   = mutableListOf<String>()
        val positive = mutableListOf<String>()
        var score    = 1.0f

        val leftShoulder  = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftWrist     = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist    = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftElbow     = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow    = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

        if (leftShoulder == null || rightShoulder == null ||
            leftWrist == null || rightWrist == null ||
            leftElbow == null || rightElbow == null) {
            return PostureFeedback(0.5f, false, listOf("Full upper body must be visible"), emptyList(), ExerciseType.BAMBOO_STICK)
        }

        // Check: both wrists at similar height (holding stick horizontally)
        val wristHeightDiff = abs(leftWrist.position.y - rightWrist.position.y)
        if (wristHeightDiff > 50) {
            issues.add("Keep both hands at the same height — level the stick")
            score -= 0.25f
        } else {
            positive.add("Hands at even height — good stick level")
        }

        // Check: wrists at or near shoulder height
        val avgWristY     = (leftWrist.position.y + rightWrist.position.y) / 2
        val avgShoulderY  = (leftShoulder.position.y + rightShoulder.position.y) / 2
        val heightDiff    = abs(avgWristY - avgShoulderY)
        if (heightDiff > 70) {
            issues.add("Raise hands to chest/shoulder height to hold the stick")
            score -= 0.20f
        } else {
            positive.add("Arms at correct height")
        }

        // Check: elbows not fully locked (soft bend for relaxed connection)
        val leftElbowAngle  = calculateAngle(leftShoulder.position, leftElbow.position, leftWrist.position)
        val rightElbowAngle = calculateAngle(rightShoulder.position, rightElbow.position, rightWrist.position)

        if (leftElbowAngle > 175f || rightElbowAngle > 175f) {
            issues.add("Soften the elbow — don't lock the arms fully")
            score -= 0.15f
        } else {
            positive.add("Soft elbow bend — relaxed connection")
        }

        // Check: shoulder line level
        val shoulderLevelDiff = abs(leftShoulder.position.y - rightShoulder.position.y)
        if (shoulderLevelDiff > 35) {
            issues.add("Level the shoulders — avoid tilting to one side")
            score -= 0.15f
        } else {
            positive.add("Shoulders level and balanced")
        }

        score = score.coerceIn(0f, 1f)
        return PostureFeedback(score, score >= 0.65f, issues, positive, ExerciseType.BAMBOO_STICK)
    }

    // ── SOLO CENTER (HARA) ────────────────────────────────────────────────────
    // Checks: upright posture, shoulders back/relaxed, chin parallel to floor
    private fun analyzeSoloCenter(pose: Pose): PostureFeedback {
        val issues   = mutableListOf<String>()
        val positive = mutableListOf<String>()
        var score    = 1.0f

        val leftShoulder  = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftHip       = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip      = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val nose          = pose.getPoseLandmark(PoseLandmark.NOSE)
        val leftEar       = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
        val rightEar      = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)

        if (leftShoulder == null || rightShoulder == null ||
            leftHip == null || rightHip == null) {
            return PostureFeedback(0.5f, false, listOf("Full body should be visible"), emptyList(), ExerciseType.SOLO_CENTER)
        }

        // Check: head level (ears at same height → chin parallel to floor)
        if (leftEar != null && rightEar != null) {
            val earDiff = abs(leftEar.position.y - rightEar.position.y)
            if (earDiff > 20) {
                issues.add("Level your head — chin parallel to the floor")
                score -= 0.15f
            } else {
                positive.add("Head level, chin parallel to floor")
            }
        }

        // Check: spine alignment (shoulder center should align with hip center vertically)
        val shoulderCenterY = (leftShoulder.position.y + rightShoulder.position.y) / 2
        val hipCenterY      = (leftHip.position.y + rightHip.position.y) / 2
        val shoulderCenterX = (leftShoulder.position.x + rightShoulder.position.x) / 2
        val hipCenterX      = (leftHip.position.x + rightHip.position.x) / 2

        val spineDeviation  = abs(shoulderCenterX - hipCenterX)
        if (spineDeviation > 40) {
            issues.add("Align spine — shoulders should be over hips")
            score -= 0.20f
        } else {
            positive.add("Spine aligned over center")
        }

        // Check: shoulders not raised/hunched (shoulder y should be well above hip y)
        val torsoLength     = abs(shoulderCenterY - hipCenterY)
        val shoulderWidth   = abs(leftShoulder.position.x - rightShoulder.position.x)
        // If torso is very short relative to shoulder width, person is hunching
        val postureRatio    = if (shoulderWidth > 0) torsoLength / shoulderWidth else 1f
        if (postureRatio < 0.8f) {
            issues.add("Roll shoulders back and stand taller")
            score -= 0.20f
        } else {
            positive.add("Shoulders back, posture open")
        }

        // Check: shoulders level
        val shoulderLevelDiff = abs(leftShoulder.position.y - rightShoulder.position.y)
        if (shoulderLevelDiff > 30) {
            issues.add("Relax and level the shoulders")
            score -= 0.15f
        } else {
            positive.add("Shoulders relaxed and level")
        }

        score = score.coerceIn(0f, 1f)
        return PostureFeedback(score, score >= 0.65f, issues, positive, ExerciseType.SOLO_CENTER)
    }

    // ── Math helpers ──────────────────────────────────────────────────────────
    private fun calculateAngle(
        a: android.graphics.PointF,
        b: android.graphics.PointF,   // vertex
        c: android.graphics.PointF
    ): Float {
        val radians = atan2(
            (c.y - b.y).toDouble(), (c.x - b.x).toDouble()
        ) - atan2(
            (a.y - b.y).toDouble(), (a.x - b.x).toDouble()
        )
        var angle = Math.toDegrees(radians).toFloat()
        if (angle < 0) angle += 360f
        if (angle > 180) angle = 360f - angle
        return angle
    }

    private fun distance(a: android.graphics.PointF, b: android.graphics.PointF): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}
