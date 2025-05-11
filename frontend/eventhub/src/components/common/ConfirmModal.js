import {motion} from "framer-motion";
import React from "react";
import "../../css/ModalWindow.css";

const ConfirmModal = ({headerText, mainText, cancelText = "Отмена", confirmText, isOpen, onClose, onConfirm}) => {
    if (!isOpen) return null;
    return (
        <div className="modal-overlay">
            <motion.div
                className="modal-content"
                initial={{scale: 0.9, opacity: 0}}
                animate={{scale: 1, opacity: 1}}
                exit={{scale: 0.9, opacity: 0}}
            >
                <h3>{headerText}</h3>
                <p>{mainText}</p>
                <div className="modal-buttons">
                    <button className="modal-button cancel" onClick={onClose}>
                        {cancelText}
                    </button>
                    <button className="modal-button confirm" onClick={onConfirm}>
                        {confirmText}
                    </button>
                </div>
            </motion.div>
        </div>
    );
};
export default ConfirmModal