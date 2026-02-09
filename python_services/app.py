import os
import io
import json
import logging

try:
    import cv2
    import numpy as np
    import face_recognition
    AI_AVAILABLE = True
except ImportError as e:
    print(f"Warning: AI definitions missing: {e}")
    AI_AVAILABLE = False
    
from flask import Flask, request, jsonify

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)

# Configuration
ENCODING_MODEL_VERSION = "dlib_resnet_v1" if AI_AVAILABLE else "mock_v1"

@app.route('/enroll-face', methods=['POST'])
def enroll_face():
    """
    Endpoint to process a student's face image and return the encoding.
    Expected Input: 'image' file in multipart/form-data.
    Returns: JSON with 'status', 'face_encoding', 'model_version', or 'error'.
    """
    if 'image' not in request.files:
        return jsonify({"status": "error", "message": "No image part"}), 400
    
    file = request.files['image']
    if file.filename == '':
        return jsonify({"status": "error", "message": "No selected file"}), 400

    if not AI_AVAILABLE:
        # Mock response for testing when libraries are missing
        logging.warning("AI libraries not available. Returning mock encoding.")
        mock_encoding = [0.1] * 128
        return jsonify({
            "status": "success",
            "face_encoding": json.dumps(mock_encoding),
            "model_version": ENCODING_MODEL_VERSION,
            "warning": "AI libraries missing, using mock data"
        })

    try:
        # Read image to numpy array
        file_bytes = np.frombuffer(file.read(), np.uint8)
        image = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)

        if image is None:
             return jsonify({"status": "error", "message": "Invalid image format"}), 400

        # Convert to RGB (face_recognition uses RGB)
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        
        # Quality Check: Resolution (Min 100x100)
        height, width = image.shape[:2]
        if height < 100 or width < 100:
             return jsonify({"status": "error", "message": "Image resolution too low (min 100x100)"}), 400

        # Detect faces
        face_locations = face_recognition.face_locations(rgb_image)
        
        if len(face_locations) == 0:
            return jsonify({"status": "error", "message": "No face detected"}), 400
        
        if len(face_locations) > 1:
            return jsonify({"status": "error", "message": "Multiple faces detected. Please upload an image with a single face."}), 400

        # Generate encoding
        face_encodings = face_recognition.face_encodings(rgb_image, face_locations)
        
        if not face_encodings:
             return jsonify({"status": "error", "message": "Could not encode face"}), 400

        # We only have one face
        encoding = face_encodings[0].tolist()

        response = jsonify({
            "status": "success",
            "face_encoding": json.dumps(encoding),
            "model_version": ENCODING_MODEL_VERSION
        })
        response.headers['X-AI-MODE'] = "REAL" if AI_AVAILABLE else "MOCK"
        return response

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/recognize-class', methods=['POST'])
def recognize_class():
    """
    Endpoint to recognize multiple faces in a classroom photo.
    Input: 'image' file, 'known_encodings' JSON string {student_id: encoding_list, ...}
    Returns: JSON with 'present_students' [student_id, ...]
    """
    if 'image' not in request.files or 'known_encodings' not in request.form:
        return jsonify({"status": "error", "message": "Missing image or encodings"}), 400
    
    file = request.files['image']
    known_encodings_map = json.loads(request.form['known_encodings']) # {id: encoding_list}

    if not AI_AVAILABLE:
        logging.warning("AI libraries not available. Returning mock attendance.")
        # Return all students as present for testing
        return jsonify({
            "status": "success",
            "present_students": list(known_encodings_map.keys())
        })

    try:
        # Load image
        file_bytes = np.frombuffer(file.read(), np.uint8)
        image = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        # Detect all faces
        face_locations = face_recognition.face_locations(rgb_image)
        face_encodings = face_recognition.face_encodings(rgb_image, face_locations)

        present_students = []

        # Prepare known faces for bulk comparison
        known_ids = []
        known_faces_list = []
        
        for s_id, encoding in known_encodings_map.items():
            known_ids.append(s_id)
            known_faces_list.append(encoding)

        if not known_faces_list:
            return jsonify({"status": "error", "message": "No known encodings provided"}), 400

        # Compare
        for encoding in face_encodings:
            matches = face_recognition.compare_faces(known_faces_list, encoding, tolerance=0.6)
            face_distances = face_recognition.face_distance(known_faces_list, encoding)
            
            best_match_index = np.argmin(face_distances)
            if matches[best_match_index]:
                student_id = known_ids[best_match_index]
                if student_id not in present_students:
                    present_students.append(student_id)

        return jsonify({
            "status": "success",
            "present_students": present_students
        })

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

def get_ear(eye_points):
    """
    Compute Eye Aspect Ratio (EAR).
    Formula: (||p2-p6|| + ||p3-p5||) / (2 * ||p1-p4||)
    """
    A = np.linalg.norm(np.array(eye_points[1]) - np.array(eye_points[5]))
    B = np.linalg.norm(np.array(eye_points[2]) - np.array(eye_points[4]))
    C = np.linalg.norm(np.array(eye_points[0]) - np.array(eye_points[3]))
    ear = (A + B) / (2.0 * C)
    return ear

@app.route('/verify-live-student', methods=['POST'])
def verify_live_student():
    """
    Endpoint to verify identity and presence (with blink check).
    Input: 'image' (snapshot or frame), optional 'reference_encoding' JSON string.
    Returns: JSON with 'verified', 'liveness', 'blink_detected', etc.
    """
    if 'image' not in request.files:
        return jsonify({"status": "error", "message": "No image"}), 400
    
    file = request.files['image']
    reference_encoding = request.form.get('reference_encoding')

    if not AI_AVAILABLE:
        logging.warning("AI libraries not available. Returning mock success.")
        return jsonify({
            "verified": True,
            "liveness": "mock_success",
            "blink_detected": True,
            "match": True if reference_encoding else None
        })

    try:
        # Load image
        file_bytes = np.frombuffer(file.read(), np.uint8)
        image = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        
        # Detect face
        face_locations = face_recognition.face_locations(rgb_image)
        
        if not face_locations:
            return jsonify({
                "verified": False,
                "liveness": "fail",
                "message": "No face detected. Please look at the camera."
            })

        # Blink Detection (EAR)
        landmarks = face_recognition.face_landmarks(rgb_image, face_locations)
        blink_detected = False
        ear_value = 0.0

        if landmarks:
            left_eye = landmarks[0]['left_eye']
            right_eye = landmarks[0]['right_eye']
            
            left_ear = get_ear(left_eye)
            right_ear = get_ear(right_eye)
            ear_value = (left_ear + right_ear) / 2.0
            
            # Threshold for closed eyes is typically around 0.2 - 0.25
            if ear_value < 0.22:
                blink_detected = True

        # If identity verification is requested
        if reference_encoding:
            try:
                ref_list = json.loads(reference_encoding)
                live_encodings = face_recognition.face_encodings(rgb_image, face_locations)
                
                if not live_encodings:
                     return jsonify({"verified": False, "liveness": "fail", "message": "Could not extract face features"})

                # Compare
                matches = face_recognition.compare_faces([ref_list], live_encodings[0], tolerance=0.6)
                
                if matches[0]:
                    return jsonify({
                        "verified": True,
                        "liveness": "face_matched",
                        "match": True,
                        "blink_detected": blink_detected,
                        "ear": ear_value,
                        "message": "Identity verified!" if blink_detected else "Face matched. Please blink to confirm liveness."
                    })
                else:
                    return jsonify({
                        "verified": False,
                        "liveness": "mismatch",
                        "match": False,
                        "message": "Face does not match your registered profile"
                    })
            except Exception as e:
                 logging.error(f"Comparison error: {e}")
                 return jsonify({"verified": False, "message": "Internal verification error"})

        # Only presence
        return jsonify({
            "verified": True,
            "liveness": "face_detected",
            "blink_detected": blink_detected,
            "ear": ear_value
        })

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy"})

if __name__ == '__main__':
    # Run on localhost:5000
    app.run(host='0.0.0.0', port=5000, debug=False)

